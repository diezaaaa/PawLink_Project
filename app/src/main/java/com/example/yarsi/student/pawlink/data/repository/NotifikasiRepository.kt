package com.example.yarsi.student.pawlink.data.repository

import android.util.Log
import com.example.yarsi.student.pawlink.config.AppWriteProvider
import io.appwrite.Query

class NotifikasiRepository {

    companion object {
        private const val DATABASE_ID = "6a152d050026fd474a91"
        private const val ADOPTIONS_COLLECTION_ID = "adoptions"
        private const val REPORTS_COLLECTION_ID = "reports"
        private const val ANIMALS_COLLECTION_ID = "animals"
        private const val TAG = "PawLink"
    }

    private val database = AppWriteProvider.databases

    // Ambil semua notifikasi untuk user tertentu
    // Notifikasi adopsi: hewan milik user yang ada pengajuan adopsinya
    // Notifikasi laporan: hewan hilang milik user yang dilaporkan ditemukan
    suspend fun getSemuaNotifikasi(userId: String): Result<List<NotifikasiModel>> {
        return try {
            val result = mutableListOf<NotifikasiModel>()

            // Ambil hewan milik user
            val hewanResponse = database.listDocuments(
                databaseId = DATABASE_ID,
                collectionId = ANIMALS_COLLECTION_ID,
                queries = listOf(Query.equal("user_id", userId), Query.limit(50))
            )
            val hewanMap = hewanResponse.documents.associate { it.id to (it.data["name"]?.toString() ?: "Hewan") }

            // Notifikasi dari adoptions
            if (hewanMap.isNotEmpty()) {
                try {
                    val adoptionResponse = database.listDocuments(
                        databaseId = DATABASE_ID,
                        collectionId = ADOPTIONS_COLLECTION_ID,
                        queries = listOf(
                            Query.equal("owner_id", userId),
                            Query.orderDesc("\$createdAt"),
                            Query.limit(30)
                        )
                    )
                    adoptionResponse.documents.forEach { doc ->
                        val data = doc.data
                        val status = data["status"]?.toString() ?: "pending"
                        val animalId = data["animal_id"]?.toString() ?: ""
                        val animalName = hewanMap[animalId] ?: "Hewan"

                        val (tipe, judul, pesan) = when (status) {
                            "approved" -> Triple(
                                TipeNotifikasi.ADOPSI_DITERIMA,
                                "Adopsi Diterima ✅",
                                "Pengajuan adopsi untuk $animalName telah disetujui."
                            )
                            "rejected" -> Triple(
                                TipeNotifikasi.ADOPSI_DITOLAK,
                                "Adopsi Ditolak ❌",
                                "Pengajuan adopsi untuk $animalName ditolak."
                            )
                            else -> Triple(
                                TipeNotifikasi.ADOPSI_DITERIMA,
                                "Pengajuan Adopsi Baru 🐾",
                                "Ada seseorang yang ingin mengadopsi $animalName."
                            )
                        }

                        result.add(
                            NotifikasiModel(
                                id = doc.id,
                                tipe = tipe,
                                judul = judul,
                                pesan = pesan,
                                animalId = animalId,
                                animalName = animalName,
                                status = status,
                                waktu = doc.createdAt,
                                sudahDibaca = false
                            )
                        )
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Gagal ambil adoptions: ${e.message}")
                }
            }

            // Notifikasi dari reports (hewan hilang milik user ditemukan)
            if (hewanMap.isNotEmpty()) {
                try {
                    val reportResponse = database.listDocuments(
                        databaseId = DATABASE_ID,
                        collectionId = REPORTS_COLLECTION_ID,
                        queries = listOf(
                            Query.orderDesc("\$createdAt"),
                            Query.limit(30)
                        )
                    )
                    reportResponse.documents.forEach { doc ->
                        val data = doc.data
                        val animalId = data["animal_id"]?.toString() ?: ""
                        // Hanya tampilkan kalau hewan ini milik user
                        if (hewanMap.containsKey(animalId)) {
                            val animalName = hewanMap[animalId] ?: "Hewan"
                            result.add(
                                NotifikasiModel(
                                    id = doc.id,
                                    tipe = TipeNotifikasi.HEWAN_DITEMUKAN,
                                    judul = "Hewan Ditemukan 📍",
                                    pesan = data["description"]?.toString()
                                        ?: "$animalName dilaporkan ditemukan di suatu lokasi.",
                                    animalId = animalId,
                                    animalName = animalName,
                                    status = "ditemukan",
                                    waktu = doc.createdAt,
                                    sudahDibaca = false
                                )
                            )
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Gagal ambil reports: ${e.message}")
                }
            }

            // Notifikasi postingan baru dari hewan lain (bukan milik user)
            try {
                val postinganResponse = database.listDocuments(
                    databaseId = DATABASE_ID,
                    collectionId = ANIMALS_COLLECTION_ID,
                    queries = listOf(
                        Query.orderDesc("\$createdAt"),
                        Query.limit(10)
                    )
                )
                postinganResponse.documents
                    .filter { it.data["user_id"]?.toString() != userId }
                    .take(5)
                    .forEach { doc ->
                        val data = doc.data
                        val name = data["name"]?.toString() ?: "Hewan baru"
                        val type = data["type"]?.toString() ?: ""
                        result.add(
                            NotifikasiModel(
                                id = "post_${doc.id}",
                                tipe = TipeNotifikasi.POSTINGAN_BARU,
                                judul = "Postingan Baru 🆕",
                                pesan = "$name ($type) baru saja diposting di sekitar kamu.",
                                animalId = doc.id,
                                animalName = name,
                                status = data["status"]?.toString() ?: "",
                                waktu = doc.createdAt,
                                sudahDibaca = false
                            )
                        )
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Gagal ambil postingan baru: ${e.message}")
            }

            // Urutkan dari terbaru
            val sorted = result.sortedByDescending { it.waktu }
            Log.d(TAG, "Total notifikasi: ${sorted.size}")
            Result.success(sorted)

        } catch (e: Exception) {
            Log.e(TAG, "Gagal ambil notifikasi: ${e.message}")
            Result.failure(e)
        }
    }
}

// ── Tipe notifikasi ───────────────────────────────────────────────────────────

enum class TipeNotifikasi {
    ADOPSI_DITERIMA,
    ADOPSI_DITOLAK,
    HEWAN_DITEMUKAN,
    POSTINGAN_BARU
}

// ── Model ─────────────────────────────────────────────────────────────────────

data class NotifikasiModel(
    val id: String,
    val tipe: TipeNotifikasi,
    val judul: String,
    val pesan: String,
    val animalId: String,
    val animalName: String,
    val status: String,
    val waktu: String,
    val sudahDibaca: Boolean = false
)