package com.example.tp_loomo.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tp_loomo.data.remote.api.supabase
import com.example.tp_loomo.data.remote.model.Project
import com.example.tp_loomo.data.repository.AdminRepository
import com.example.tp_loomo.data.remote.model.UserProfile
import com.example.tp_loomo.data.repository.ProjectRepository
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.launch

class AdminViewModel : ViewModel() {
    private val repository = AdminRepository()
    private val projectRepository = ProjectRepository()

    var isLoading by mutableStateOf(false)
    var usersList by mutableStateOf<List<UserProfile>>(emptyList())
    var allProjectsList by mutableStateOf<List<Project>>(emptyList())
        private set

    var projectProgressMap by mutableStateOf<Map<Int, Int>>(emptyMap())
        private set
    var totalSystemUsers by mutableIntStateOf(0)
        private set

    fun loadAllProjects() {
        viewModelScope.launch {
            isLoading = true
            try {
                val projects = projectRepository.getAllProjects()
                allProjectsList = projects

                val progressMap = mutableMapOf<Int, Int>()
                for (project in projects) {
                    val tasks = projectRepository.getProjectTasks(project.id)
                    val totalTasks = tasks.size
                    val completedTasks = tasks.count { it.status?.lowercase() in listOf("completed", "concluída", "concluido") || it.completion_rate == 100 }
                    progressMap[project.id] = if (totalTasks > 0) {
                        ((completedTasks.toFloat() / totalTasks) * 100).toInt()
                    } else {
                        0
                    }
                }
                projectProgressMap = progressMap
            } catch (e: Exception) {
                allProjectsList = emptyList()
                projectProgressMap = emptyMap()
            } finally {
                isLoading = false
            }
        }
    }

    // Vai à BD contar TODA a gente
    fun loadTotalUsersCount() {
        viewModelScope.launch {
            try {
                // Traz apenas os IDs para ser ultra-rápido e leve
                val allUsers = supabase.postgrest["profiles"]
                    .select(columns = Columns.list("id"))
                    .decodeList<UserProfile>()
                totalSystemUsers = allUsers.size
            } catch (e: Exception) {
                totalSystemUsers = 0
            }
        }
    }

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

    // Função de criação de projeto
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