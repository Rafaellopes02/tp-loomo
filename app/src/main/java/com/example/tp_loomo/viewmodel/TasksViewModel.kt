package com.example.tp_loomo.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tp_loomo.data.remote.model.Task
import com.example.tp_loomo.data.repository.TaskRepository
import kotlinx.coroutines.launch

class TasksViewModel : ViewModel() {
    private val repository = TaskRepository()

    var tasks by mutableStateOf<List<Task>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    // RF12 — Carregar as tarefas de um projeto
    fun loadProjectTasks(projectId: Int) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                tasks = repository.getTasksByProject(projectId)
            } catch (e: Exception) {
                errorMessage = e.message
                tasks = emptyList()
            } finally {
                isLoading = false
            }
        }
    }

    // RF10 — Criar uma nova tarefa e recarregar a lista
    fun createTask(
        projectId: Int,
        title: String,
        description: String?,
        dueDate: String?,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val success = repository.createTask(projectId, title, description, dueDate)
            if (success) loadProjectTasks(projectId)
            onResult(success)
        }
    }

    // RF19 — Marcar tarefa como concluída e recarregar
    fun markTaskAsCompleted(taskId: Int, projectId: Int) {
        viewModelScope.launch {
            val success = repository.markAsCompleted(taskId)
            if (success) loadProjectTasks(projectId)
        }
    }
}