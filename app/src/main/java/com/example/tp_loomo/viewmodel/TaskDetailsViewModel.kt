package com.example.tp_loomo.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tp_loomo.data.remote.api.supabase
import com.example.tp_loomo.data.remote.model.Project
import com.example.tp_loomo.data.remote.model.Task
import com.example.tp_loomo.data.repository.ProjectRepository
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch

class TaskDetailsViewModel : ViewModel() {
    private val repository = ProjectRepository()

    var task by mutableStateOf<Task?>(null)
        private set

    var project by mutableStateOf<Project?>(null)
        private set

    var isLoading by mutableStateOf(true)
        private set

    var isAssignedToCurrentUser by mutableStateOf(false)
        private set

    fun loadTaskDetails(taskId: Int) {
        viewModelScope.launch {
            isLoading = true
            try {
                // 1. Carrega a tarefa e o projeto
                val loadedTask = repository.getTaskById(taskId)
                task = loadedTask

                if (loadedTask != null) {
                    project = repository.getProjectById(loadedTask.project_id)
                }

                // 2. Lógica de Permissões
                val currentUserId = supabase.auth.currentUserOrNull()?.id
                if (currentUserId != null) {
                    // Verifica se é o Gestor Supremo do Projeto
                    val isManager = project?.project_manager_id == currentUserId
                    // Verifica se é apenas um membro atribuído à tarefa
                    val isAssigned = repository.isUserAssignedToTask(taskId, currentUserId)

                    // Mostra o botão se for O GESTOR -OU- SE ESTIVER ATRIBUÍDO
                    isAssignedToCurrentUser = isManager || isAssigned
                }

            } catch (e: Exception) {
                task = null
                project = null
                isAssignedToCurrentUser = false
            } finally {
                isLoading = false
            }
        }
    }

    fun completeTask(taskId: Int, onSuccess: () -> Unit) {
        // Futuramente faremos o update da BD aqui
        onSuccess()
    }
}