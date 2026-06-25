package com.example.yarsi.student.pawlink.data.repository

import com.example.yarsi.student.pawlink.config.AppWriteProvider
import io.appwrite.Query

class HewanRepository {

    private val database = AppWriteProvider.databases

    companion object {
        const val DATABASE_ID = "6a152d050026fd474a91"
        const val COLLECTION_ID = "animals"
    }

    suspend fun getSemuaHewan(): Result<List<HewanModel>> {
        return try {
            val response = database.listDocuments(
                DATABASE_ID,
                COLLECTION_ID,
                queries = listOf(
                    Query.orderDesc("\$createdAt"),
                    Query.limit(50)
                )
            )
            val list = response.documents.map { it.toHewanModel() }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getHewanByStatus(status: String): Result<List<HewanModel>> {
        return try {
            val response = database.listDocuments(
                databaseId = DATABASE_ID,
                collectionId = COLLECTION_ID,
                queries = listOf(
                    Query.equal("status", status),
                    Query.orderDesc("\$createdAt"),
                    Query.limit(50)
                )
            )
            val list = response.documents.map { it.toHewanModel() }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getHewanByType(type: String): Result<List<HewanModel>> {
        return try {
            val response = database.listDocuments(
                DATABASE_ID,
                COLLECTION_ID,
                queries = listOf(
                    Query.equal("type", type),
                    Query.orderDesc("\$createdAt"),
                    Query.limit(50)
                )
            )
            val list = response.documents.map { it.toHewanModel() }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getHewanById(userId: String): Result<List<HewanModel>> {
        return try {
            val response = database.listDocuments(
                DATABASE_ID,
                COLLECTION_ID,
                queries = listOf(
                    Query.equal("user_id", userId)
                )
            )
            val list = response.documents.map { it.toHewanModel() }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getHewanByDocumentId(documentId: String): Result<HewanModel> {
        return try {
            val doc = database.getDocument(
                databaseId = DATABASE_ID,
                collectionId = COLLECTION_ID,
                documentId = documentId
            )
            Result.success(doc.toHewanModel())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun tambahHewan(hewan: HewanModel): Result<String> {
        return try {
            val response = database.createDocument(
                DATABASE_ID,
                COLLECTION_ID,
                documentId = io.appwrite.ID.unique(),
                data = hewan.toMap()
            )
            Result.success(response.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateStatusHewan(id: String, status: String): Result<Unit> {
        return try {
            database.updateDocument(
                databaseId = DATABASE_ID,
                collectionId = COLLECTION_ID,
                documentId = id,
                data = mapOf("status" to status)
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAktivitasTerbaru(): Result<List<HewanModel>> {
        return try {
            android.util.Log.d("PawLink", "getAktivitasTerbaru dipanggil")
            val response = database.listDocuments(
                DATABASE_ID,
                COLLECTION_ID,
                queries = listOf(
                    Query.orderDesc("\$createdAt"),
                    Query.limit(10)
                )
            )
            android.util.Log.d("PawLink", "Aktivitas result: ${response.documents.size} docs")
            val list = response.documents.map { it.toHewanModel() }
            Result.success(list)
        } catch (e: Exception) {
            android.util.Log.e("PawLink", "getAktivitasTerbaru error: ${e.message}")
            Result.failure(e)
        }
    }
}

data class HewanModel(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val type: String = "",
    val breed: String = "",
    val age: String = "",
    val gender: String = "",
    val description: String = "",
    val status: String = "",       // "tersedia", "hilang", "ditemukan", "teradopsi"
    val postType: String = "",     // "adopsi", "hilang"
    val location: String = "",
    val photoUrl: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val createdAt: String = ""
)

fun io.appwrite.models.Document<Map<String, Any>>.toHewanModel(): HewanModel {
    val data = this.data
    return HewanModel(
        id          = this.id,
        userId      = data["user_id"]?.toString() ?: "",
        name        = data["name"]?.toString() ?: "",
        type        = data["type"]?.toString() ?: "",
        breed       = data["breed"]?.toString() ?: "",
        age         = data["age"]?.toString() ?: "",
        gender      = data["gender"]?.toString() ?: "",
        description = data["description"]?.toString() ?: "",
        status      = data["status"]?.toString() ?: "",
        postType    = data["post_type"]?.toString() ?: "",
        location    = data["location"]?.toString() ?: "",
        photoUrl    = data["photo_url"]?.toString() ?: "",
        latitude    = data["latitude"]?.toString()?.toDoubleOrNull() ?: 0.0,
        longitude   = data["longitude"]?.toString()?.toDoubleOrNull() ?: 0.0,
        createdAt   = this.createdAt
    )
}

fun HewanModel.toMap(): Map<String, Any> = mapOf(
    "user_id"     to userId,
    "name"        to name,
    "type"        to type,
    "breed"       to breed,
    "age"         to age,
    "gender"      to gender,
    "description" to description,
    "status"      to status,
    "post_type"   to postType,
    "location"    to location,
    "photo_url"   to photoUrl,
    "latitude"    to latitude,
    "longitude"   to longitude
)