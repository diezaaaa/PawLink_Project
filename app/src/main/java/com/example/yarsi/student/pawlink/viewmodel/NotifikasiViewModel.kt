package com.example.yarsi.student.pawlink.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yarsi.student.pawlink.data.repository.NotifikasiModel
import com.example.yarsi.student.pawlink.data.repository.NotifikasiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotifikasiUiState(
    val isLoading: Boolean = false,
    val belumDibaca: List<NotifikasiModel> = emptyList(),
    val sudahDibaca: List<NotifikasiModel> = emptyList(),
    val errorMessage: String? = null
)

class NotifikasiViewModel : ViewModel() {

    private val repository = NotifikasiRepository()

    private val _uiState = MutableStateFlow(NotifikasiUiState())
    val uiState: StateFlow<NotifikasiUiState> = _uiState.asStateFlow()

    // State lokal untuk track yang sudah dibaca (in-memory, karena tidak ada tabel notifikasi)
    private val dibacaIds = mutableSetOf<String>()

    fun loadNotifikasi(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = repository.getSemuaNotifikasi(userId)

            result
                .onSuccess { list ->
                    val updated = list.map { it.copy(sudahDibaca = dibacaIds.contains(it.id)) }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            belumDibaca = updated.filter { n -> !n.sudahDibaca },
                            sudahDibaca = updated.filter { n -> n.sudahDibaca }
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message ?: "Gagal memuat notifikasi."
                        )
                    }
                }
        }
    }

    // Tandai satu notifikasi sudah dibaca
    fun tandaiDibaca(id: String) {
        dibacaIds.add(id)
        _uiState.update { state ->
            val semua = (state.belumDibaca + state.sudahDibaca)
                .map { if (it.id == id) it.copy(sudahDibaca = true) else it }
            state.copy(
                belumDibaca = semua.filter { !it.sudahDibaca },
                sudahDibaca = semua.filter { it.sudahDibaca }
            )
        }
    }

    // Tandai semua sudah dibaca
    fun tandaiSemuaDibaca() {
        _uiState.update { state ->
            val semua = (state.belumDibaca + state.sudahDibaca)
                .map { it.copy(sudahDibaca = true) }
            semua.forEach { dibacaIds.add(it.id) }
            state.copy(
                belumDibaca = emptyList(),
                sudahDibaca = semua
            )
        }
    }

    // Hapus satu notifikasi
    fun hapusNotifikasi(id: String) {
        _uiState.update { state ->
            state.copy(
                belumDibaca = state.belumDibaca.filter { it.id != id },
                sudahDibaca = state.sudahDibaca.filter { it.id != id }
            )
        }
    }

    // Hapus semua notifikasi
    fun hapusSemua() {
        dibacaIds.clear()
        _uiState.update {
            it.copy(belumDibaca = emptyList(), sudahDibaca = emptyList())
        }
    }
}