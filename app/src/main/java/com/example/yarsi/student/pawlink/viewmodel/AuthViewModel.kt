package com.example.yarsi.student.pawlink.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yarsi.student.pawlink.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.net.Uri

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

    private val _userRole = MutableStateFlow("pencari") // default pencari
    val userRole: StateFlow<String> = _userRole.asStateFlow()

    private val _userEmail = MutableStateFlow("")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                android.util.Log.d("PawLink", "Login dipanggil: $email")
                val result = repository.login(email, password)
                result
                    .onSuccess {
                        android.util.Log.d("PawLink", "Login berhasil!")
                        fetchCurrentUser()
                        _userEmail.value = email
                        _isLoginSuccess.value = true
                    }
                    .onFailure {
                        android.util.Log.e("PawLink", "Login gagal: ${it.message}")
                        _errorMessage.value = it.message
                    }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun register(context: Context, name: String, email: String, password: String, phone: String, city: String, role: String, photoUri: Uri?) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                android.util.Log.d("PawLink", "ViewModel.register() dipanggil - photoUri = $photoUri")

                val result = repository.register(
                    context = context,
                    name = name,
                    email = email,
                    password = password,
                    phone = phone,
                    city = city,
                    role = role,
                    photoUri = photoUri
                )

                result.onSuccess {
                    android.util.Log.d("PawLink", "ViewModel: register sukses - $it")
                    _userName.value = name
                    _userEmail.value = email
                    _isRegisterSuccess.value = true
                }.onFailure {
                    _errorMessage.value = it.message
                    android.util.Log.e("PawLink", "ViewModel: register failed", it)
                }

            } catch (e: Exception) {
                _errorMessage.value = e.message
                android.util.Log.e("PawLink", "ViewModel: register crash", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _isLoading.value = true

            val result = repository.logout()

            result.onSuccess {
                _isLoginSuccess.value = false
                _userName.value = "Pengguna"
                _userEmail.value = ""
            }.onFailure {
                _errorMessage.value = it.message
            }

            _isLoading.value = false
        }
    }

    fun fetchCurrentUser() {
        viewModelScope.launch {
            val result = repository.getCurrentUser()
            result.onSuccess { (name, role) ->
                _userName.value = name.ifBlank { "Pengguna" }
                _userRole.value = role
            }
        }
    }

    fun resetState() {
        _isLoginSuccess.value = false
        _isRegisterSuccess.value = false
        _errorMessage.value = null
    }

    suspend fun getCurrentUserId(): String? {
        return repository
            .getCurrentUserId()
            .getOrNull()
    }
}