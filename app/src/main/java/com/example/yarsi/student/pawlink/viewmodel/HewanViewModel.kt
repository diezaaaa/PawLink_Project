package com.example.yarsi.student.pawlink.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yarsi.student.pawlink.data.repository.HewanModel
import com.example.yarsi.student.pawlink.data.repository.HewanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
}