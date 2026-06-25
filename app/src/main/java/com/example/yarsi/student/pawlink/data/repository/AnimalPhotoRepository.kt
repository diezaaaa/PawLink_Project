package com.example.yarsi.student.pawlink.data.repository

import com.example.yarsi.student.pawlink.config.AppWriteProvider
import io.appwrite.ID
import io.appwrite.Query

class AnimalPhotoRepository {

    companion object {
        private const val DATABASE_ID = "6a152d050026fd474a91"
        private const val COLLECTION_ID = "animal_photos"
    }

    suspend fun tambahFoto(
        animalId: String,
        fileId: String,
        url: String,
        orderIndex: Int = 1
    ): Result<Unit> {

        return try {

            AppWriteProvider.databases.createDocument(
                databaseId = DATABASE_ID,
                collectionId = COLLECTION_ID,
                documentId = ID.unique(),
                data = mapOf(
                    "animal_id" to animalId,
                    "file_id" to fileId,
                    "url" to url,
                    "order_index" to orderIndex
                )
            )

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFotoByAnimalId(animalId: String): Result<String> {
        return try {
            val response = AppWriteProvider.databases.listDocuments(
                databaseId = DATABASE_ID,
                collectionId = COLLECTION_ID,
                queries = listOf(
                    Query.equal("animal_id", animalId),
                    Query.limit(1)
                )
            )
            val url = response.documents.firstOrNull()
                ?.data?.get("url")?.toString() ?: ""
            Result.success(url)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}