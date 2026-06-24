package com.example.yarsi.student.pawlink.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yarsi.student.pawlink.data.repository.AnimalPhotoRepository
import com.example.yarsi.student.pawlink.data.repository.HewanModel
import com.example.yarsi.student.pawlink.data.repository.HewanRepository
import com.example.yarsi.student.pawlink.data.repository.StorageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import android.content.Context
import android.net.Uri
import com.example.yarsi.student.pawlink.data.repository.AuthRepository

data class HewanUiState(
    val isLoading: Boolean = false,
    val hewanList: List<HewanModel> = emptyList(),
    val selectedHewan: HewanModel? = null,
    val errorMessage: String? = null,
    val totalTersedia: Int = 0,
    val totalHilang: Int = 0,
    val totalDitemukan: Int = 0
)

class HewanViewModel : ViewModel() {
    private val repository = HewanRepository()
    private val storageRepository = StorageRepository()
    private val animalPhotoRepository = AnimalPhotoRepository()
    private val authRepository = AuthRepository()

    private val _uiState = MutableStateFlow(HewanUiState())
    val uiState: StateFlow<HewanUiState> = _uiState.asStateFlow()

    fun loadSemuaHewan() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = repository.getSemuaHewan()

            if (result.isSuccess) {
                val list = result.getOrNull() ?: emptyList()
                _uiState.update {
                    it.copy(
                        isLoading      = false,
                        hewanList      = list,
                        totalTersedia  = list.count { h -> h.status == "tersedia" },
                        totalHilang    = list.count { h -> h.status == "hilang" },
                        totalDitemukan = list.count { h -> h.status == "ditemukan" }
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading    = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Gagal memuat data hewan."
                    )
                }
            }
        }
    }

    fun publishHewan(
        context: Context,
        hewan: HewanModel,
        imageUri: Uri
    ) {
        viewModelScope.launch {

            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            try {
                val currentUserId =
                    authRepository
                        .getCurrentUserId()
                        .getOrNull()

                if (currentUserId == null) {

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "User belum login"
                        )
                    }

                    return@launch
                }

                // 1. Upload Foto ke Storage
                val uploadResult =
                    storageRepository.uploadImage(
                        context,
                        imageUri
                    )
                if (uploadResult.isFailure) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Upload foto gagal"
                        )
                    }

                    return@launch
                }
                val (fileId, url) =
                    uploadResult.getOrThrow()

                val hewanWithUserId =
                    hewan.copy(
                        userId = currentUserId
                    )

                // 2. Simpan Hewan ke animals
                val animalResult =
                    repository.tambahHewan(
                        hewanWithUserId
                    )
                if (animalResult.isFailure) {

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Gagal menyimpan data hewan"
                        )
                    }

                    return@launch
                }
                val animalId =
                    animalResult.getOrThrow()

                // 3. Simpan Foto ke animal_photos
                val photoResult =
                    animalPhotoRepository.tambahFoto(
                        animalId = animalId,
                        fileId = fileId,
                        url = url
                    )

                if (photoResult.isFailure) {

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Gagal menyimpan foto hewan"
                        )
                    }

                    return@launch
                }

                // SUCCESS
                _uiState.update {
                    it.copy(
                        isLoading = false
                    )
                }
            } catch (e: Exception) {

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message
                    )
                }
            }
        }
    }
}