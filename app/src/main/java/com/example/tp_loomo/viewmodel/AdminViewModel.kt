package com.example.tp_loomo.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tp_loomo.data.repository.AdminRepository
import com.example.tp_loomo.data.remote.model.UserProfile
import kotlinx.coroutines.launch

class AdminViewModel : ViewModel() {
    private val repository = AdminRepository()

    var isLoading by mutableStateOf(false)
    var usersList by mutableStateOf<List<UserProfile>>(emptyList())

    // Funções de carregamento para o Dialog de seleção
    fun loadUsersForSelection(isManager: Boolean) {
        viewModelScope.launch {
            isLoading = true
            try {
                val role = if (isManager) "project_manager" else "user"
                usersList = repository.getUsersByRole(role)
            } catch (e: Exception) {
                usersList = emptyList()
            } finally {
                isLoading = false
            }
        }
    }
    fun handleCreateUser(
        email: String, fullName: String, username: String, role: String,
        onSuccess: () -> Unit, onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            isLoading = true
            try {
                repository.createNewUser(email, fullName, username, role)
                onSuccess()
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Erro ao criar utilizador")
            } finally {
                isLoading = false
            }
        }
    }

    // Função de criação de projeto adaptada para o ViewModel
    fun handleCreateProject(
        name: String, desc: String, managerId: String,
        startDate: String, endDate: String?, teamIds: List<String>,
        onSuccess: (String, String, String) -> Unit, onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            isLoading = true
            try {
                repository.createProject(name, desc, managerId, startDate, endDate, teamIds)
                onSuccess(name, desc, endDate ?: "Sem_prazo")
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Erro desconhecido")
            } finally {
                isLoading = false
            }
        }
    }
}