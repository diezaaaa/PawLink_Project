package com.example.yarsi.student.pawlink.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.yarsi.student.pawlink.config.AppWriteProvider
import io.appwrite.ID
import io.appwrite.exceptions.AppwriteException
import io.appwrite.models.InputFile
import io.appwrite.models.Session

class AuthRepository {

    companion object {
        private const val DATABASE_ID = "6a152d050026fd474a91"
        private const val USERS_COLLECTION_ID = "users"
        private const val BUCKET_ID = "profile_photos"
        private const val TAG = "PawLink"
    }

    /**
     * Upload foto ke Appwrite Storage.
     * fungsi ini harus dipanggil setelah user punya session aktif,
     * karena bucket permission membutuhkan role "users" (logged in).
     */
    private suspend fun uploadPhoto(context: Context, photoUri: Uri): String {
        Log.d(TAG, "Upload Start - uri=$photoUri")

        val inputStream = context.contentResolver.openInputStream(photoUri)
            ?: throw Exception("InputStream NULL - file tidak bisa dibuka dari URI")

        val fileBytes = inputStream.use { it.readBytes() }

        Log.d(TAG, "File Size = ${fileBytes.size} bytes")

        if (fileBytes.isEmpty()) {
            throw Exception("File kosong, ukuran 0 byte")
        }

        // InputFile.fromBytes butuh mimeType eksplisit.
        // Tanpa ini, SDK Appwrite gagal deteksi tipe file
        val mimeType = context.contentResolver.getType(photoUri) ?: "image/jpeg"
        Log.d(TAG, "MIME TYPE detected = $mimeType")

        // Tentukan ekstensi file berdasarkan mimeType supaya nama file konsisten
        val extension = when (mimeType) {
            "image/png" -> "png"
            "image/webp" -> "webp"
            else -> "jpg"
        }

        val file = AppWriteProvider.storage.createFile(
            bucketId = BUCKET_ID,
            fileId = ID.unique(),
            file = InputFile.fromBytes(
                bytes = fileBytes,
                filename = "profile.$extension",
                mimeType = mimeType
            )
        )

        Log.d(TAG, "UPLOAD SUCCESS - fileId = ${file.id}")

        val fileUrl = AppWriteProvider.storage.getFileView(
            bucketId = BUCKET_ID,
            fileId = file.id
        ).toString()

        Log.d(TAG, "FILE URL = $fileUrl")

        return fileUrl
    }

    suspend fun register(
        context: Context,
        name: String,
        email: String,
        password: String,
        phone: String,
        city: String,
        role: String,
        photoUri: Uri?
    ): Result<String> {

        return try {

            Log.d(TAG, "=== REGISTER START ===")
            Log.d(TAG, "photoUri received = $photoUri")

            // Buat user
            val user = AppWriteProvider.account.create(
                userId = ID.unique(),
                email = email,
                password = password,
                name = name
            )
            Log.d(TAG, "Step 1 OK - User created: ${user.id}")

            // Buat session dulu sebelum upload,
            // karena createFile butuh user yang sudah login (role: users)
            try {
                AppWriteProvider.account.createEmailPasswordSession(
                    email = email,
                    password = password
                )
                Log.d(TAG, "Step 2 OK - Session created, user now authenticated")
            } catch (e: Exception) {
                Log.e(TAG, "Step 2 FAILED - Gagal membuat session setelah create user", e)
            }

            // Upload foto, sekarang dengan session aktif
            val photoUrl: String? = if (photoUri != null) {
                try {
                    val url = uploadPhoto(context, photoUri)
                    Log.d(TAG, "Step 3 OK - Photo uploaded: $url")
                    url
                } catch (e: AppwriteException) {
                    Log.e(TAG, "Step 3 FAILED (AppwriteException) - code=${e.code}, type=${e.type}, message=${e.message}", e)
                    null
                } catch (e: Exception) {
                    Log.e(TAG, "Step 3 FAILED (Exception) - ${e.message}", e)
                    null
                }
            } else {
                Log.d(TAG, "Step 3 SKIPPED - photoUri null, tidak ada foto yang diupload")
                null
            }

            // Simpan data-data user, termasuk photo_url
            val document = AppWriteProvider.databases.createDocument(
                databaseId = DATABASE_ID,
                collectionId = USERS_COLLECTION_ID,
                documentId = user.id,
                data = mapOf(
                    "name" to name,
                    "email" to email,
                    "phone" to phone,
                    "city" to city,
                    "role" to role,
                    "photo_url" to (photoUrl ?: "")
                )
            )

            Log.d(TAG, "Step 4 OK - Document created: ${document.id}, photo_url field = ${document.data["photo_url"]}")
            Log.d(TAG, "=== REGISTER SUCCESS ===")

            Result.success("Register berhasil")

        } catch (e: AppwriteException) {

            Log.e(TAG, "REGISTER FAILED (AppwriteException) - code=${e.code}, type=${e.type}, message=${e.message}", e)
            Result.failure(Exception(e.message))

        } catch (e: Exception) {

            Log.e(TAG, "REGISTER FAILED (General Exception)", e)
            Result.failure(e)
        }
    }

    suspend fun login(
        email: String,
        password: String
    ): Result<Session> {

        return try {

            val session = AppWriteProvider.account.createEmailPasswordSession(
                email = email,
                password = password
            )

            Log.d(TAG, "Login success")
            Result.success(session)

        } catch (e: Exception) {
            Log.e(TAG, "Login failed", e)
            Result.failure(e)
        }
    }

    suspend fun getCurrentUser(): Result<Pair<String, String>> {
        return try {
            val user = AppWriteProvider.account.get()
            val doc = AppWriteProvider.databases.getDocument(
                databaseId = "6a152d050026fd474a91",
                collectionId = "users",
                documentId = user.id
            )
            val role = doc.data["role"]?.toString()?.lowercase() ?: "pencari"
            Result.success(Pair(user.name, role))
        } catch (e: AppwriteException) {
            Result.failure(Exception(e.message))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCurrentUserId(): Result<String> {
        return try {

            val user = AppWriteProvider.account.get()

            Result.success(user.id)

        } catch (e: Exception) {

            Result.failure(e)

        }
    }

    suspend fun logout(): Result<String> {
        return try {
            AppWriteProvider.account.deleteSession("current")
            Result.success("Logout berhasil")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}