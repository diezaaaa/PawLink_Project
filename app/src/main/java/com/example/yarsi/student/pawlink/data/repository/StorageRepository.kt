package com.example.yarsi.student.pawlink.data.repository

import android.content.Context
import android.net.Uri
import com.example.yarsi.student.pawlink.config.AppWriteProvider
import io.appwrite.ID
import io.appwrite.models.InputFile

class StorageRepository {

    companion object {
        private const val BUCKET_ID = "profile_photos"
    }

    suspend fun uploadImage(
        context: Context,
        imageUri: Uri
    ): Result<Pair<String, String>> {

        return try {

            val inputStream =
                context.contentResolver.openInputStream(imageUri)
                    ?: throw Exception("File tidak ditemukan")

            val bytes = inputStream.use {
                it.readBytes()
            }

            val mimeType =
                context.contentResolver.getType(imageUri)
                    ?: "image/jpeg"

            val file = AppWriteProvider.storage.createFile(
                bucketId = BUCKET_ID,
                fileId = ID.unique(),
                file = InputFile.fromBytes(
                    bytes = bytes,
                    filename = "animal.jpg",
                    mimeType = mimeType
                )
            )

            val url = AppWriteProvider.storage.getFileView(
                bucketId = BUCKET_ID,
                fileId = file.id
            ).toString()

            Result.success(
                Pair(file.id, url)
            )

        } catch (e: Exception) {

            Result.failure(e)

        }
    }
}