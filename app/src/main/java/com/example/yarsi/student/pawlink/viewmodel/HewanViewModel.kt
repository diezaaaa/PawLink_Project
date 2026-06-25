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
import com.example.yarsi.student.pawlink.config.AppWriteProvider
import com.example.yarsi.student.pawlink.data.repository.AuthRepository
import com.example.yarsi.student.pawlink.utils.LocationHelper

data class HewanUiState(
    val isLoading: Boolean = false,
    val hewanList: List<HewanModel> = emptyList(),
    val selectedHewan: HewanModel? = null,
    val selectedHewanContact: Pair<String, String>? = null,
    val selectedHewanPhotoUrl: String = "",
    val errorMessage: String? = null,
    val totalTersedia: Int = 0,
    val totalHilang: Int = 0,
    val totalDitemukan: Int = 0,
    val isPostingSuccess: Boolean = false,
    val aktivitasList: List<HewanModel> = emptyList()
)

class HewanViewModel : ViewModel() {
    private val repository = HewanRepository()
    private val storageRepository = StorageRepository()
    private val animalPhotoRepository = AnimalPhotoRepository()
    private val authRepository = AuthRepository()

    private val _uiState = MutableStateFlow(HewanUiState())
    val uiState: StateFlow<HewanUiState> = _uiState.asStateFlow()

    private val _lokasiUser = MutableStateFlow<Pair<Double, Double>?>(null)
    private val _radiusKm = MutableStateFlow(10.0)

    private var realtimeSubscription: io.appwrite.models.RealtimeSubscription? = null

    val radiusKm: StateFlow<Double> = _radiusKm.asStateFlow()

    fun setLokasiUser(lat: Double, lon: Double) {
        _lokasiUser.value = Pair(lat, lon)
    }

    fun getHewanTerdekat(): List<HewanModel> {
        val lokasi = _lokasiUser.value ?: return _uiState.value.hewanList
        val radius = _radiusKm.value
        return _uiState.value.hewanList.filter { hewan ->
            if (hewan.latitude == 0.0 && hewan.longitude == 0.0) return@filter false
            val jarak = LocationHelper.hitungJarak(
                lokasi.first, lokasi.second,
                hewan.latitude, hewan.longitude
            )
            android.util.Log.d("JARAK", "${hewan.name}: ${"%.2f".format(jarak)} km (radius: $radius km)")
            jarak <= radius
        }
    }

    fun setRadius(km: Double) {
        _radiusKm.value = km
    }

    fun loadHewanDetail(hewanId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.getHewanByDocumentId(hewanId)
            result
                .onSuccess { hewan ->
                    _uiState.update { it.copy(isLoading = false, selectedHewan = hewan) }

                    // Ambil kontak
                    if (hewan.userId.isNotBlank()) {
                        val authRepo = AuthRepository()
                        val contactResult = authRepo.getUserById(hewan.userId)
                        contactResult.onSuccess { (name, phone) ->
                            _uiState.update { it.copy(selectedHewanContact = Pair(name, phone)) }
                        }
                    }

                    // Ambil foto ← tambah ini
                    val photoRepo = AnimalPhotoRepository()
                    val photoResult = photoRepo.getFotoByAnimalId(hewanId)
                    photoResult
                        .onSuccess { url ->
                            android.util.Log.d("PawLink", "Photo URL = $url")
                            _uiState.update { it.copy(selectedHewanPhotoUrl = url) }
                        }
                        .onFailure { error ->
                            android.util.Log.e("PawLink", "Gagal ambil foto: ${error.message}")
                        }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
        }
    }

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
                    isPostingSuccess = false,
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
                        userId = currentUserId,
                        photoUrl = url
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
                        isLoading = false,
                        isPostingSuccess = true
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

    fun resetPostingState() {
        _uiState.update { it.copy(isPostingSuccess = false, errorMessage = null) }
    }

    fun subscribeRealtimeHewan() {
        realtimeSubscription = AppWriteProvider.realtime.subscribe(
            channels = arrayOf(
                "databases.${HewanRepository.DATABASE_ID}.collections.${HewanRepository.COLLECTION_ID}.documents"
            ),
            payloadType = Map::class.java
        ) { response ->
            android.util.Log.d("PawLink", "Realtime event: ${response.events}")
            // Reload data saat ada perubahan
            loadSemuaHewan()
        }
    }

    fun unsubscribeRealtime() {
        realtimeSubscription?.close()
        realtimeSubscription = null
    }

    override fun onCleared() {
        super.onCleared()
        unsubscribeRealtime()
    }


    fun loadAktivitasTerbaru() {
        viewModelScope.launch {
            val result = repository.getAktivitasTerbaru()
            result
                .onSuccess { list ->
                    android.util.Log.d("PawLink", "Aktivitas loaded: ${list.size} items")
                    _uiState.update { it.copy(aktivitasList = list) }
                }
                .onFailure { error ->
                    android.util.Log.e("PawLink", "Gagal load aktivitas: ${error.message}")
                }
        }
    }
}