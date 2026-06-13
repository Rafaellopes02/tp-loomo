package com.example.tp_loomo.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tp_loomo.data.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()
    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    // Função de Login chamada pela UI
    fun login(emailOrUsername: String, pass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                repository.login(emailOrUsername, pass)
                onSuccess()
            } catch (e: Exception) {
                errorMessage = if (e.message == "Username não encontrado.") {
                    e.message
                } else {
                    "Credenciais inválidas. Tenta novamente."
                }
            } finally {
                isLoading = false
            }
        }
    }
    fun signUp(fullName: String, username: String, email: String, pass: String, avatarUrl: String?, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                repository.signUp(fullName, username, email, pass, avatarUrl)
                onSuccess()
            } catch (e: Exception) {
                errorMessage = "Erro ao criar conta: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }
}