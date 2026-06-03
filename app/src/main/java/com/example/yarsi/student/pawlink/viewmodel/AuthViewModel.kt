package com.example.yarsi.student.pawlink.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yarsi.student.pawlink.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()



    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isLoginSuccess = MutableStateFlow(false)
    val isLoginSuccess: StateFlow<Boolean> = _isLoginSuccess.asStateFlow()

    private val _isRegisterSuccess = MutableStateFlow(false)
    val isRegisterSuccess: StateFlow<Boolean> = _isRegisterSuccess.asStateFlow()

    private val _userName = MutableStateFlow("Pengguna")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userEmail = MutableStateFlow("")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                android.util.Log.d("PawLink", "Login dipanggil: $email") // ← tambah ini
                val result = repository.login(email, password)
                result
                    .onSuccess {
                        android.util.Log.d("PawLink", "Login berhasil!") // ← tambah ini
                        fetchCurrentUser()
                        _isLoginSuccess.value = true
                    }
                    .onFailure {
                        android.util.Log.e("PawLink", "Login gagal: ${it.message}") // ← tambah ini
                        _errorMessage.value = it.message
                    }
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val result = repository.register(name, email, password)
                result
                    .onSuccess {
                        _userName.value = name
                        _userEmail.value = email
                        _isRegisterSuccess.value = true
                    }
                    .onFailure {
                        _errorMessage.value = it.message
                    }
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun fetchCurrentUser() {
        viewModelScope.launch {
            val result = repository.getCurrentUser()
            result.onSuccess { name ->
                _userName.value = name.ifBlank { "Pengguna" }
            }
        }
    }

    fun refreshUserData() {
        viewModelScope.launch {
            val result = repository.getCurrentUser()
            result.onSuccess { name ->
                _userName.value = name.ifBlank { "Pengguna" }
            }
        }
    }
    fun resetState() {

        _isLoginSuccess.value = false
        _isRegisterSuccess.value = false
        _errorMessage.value = null
    }

}