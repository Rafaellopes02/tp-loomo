package com.example.tp_loomo.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tp_loomo.data.remote.model.Project
import com.example.tp_loomo.data.remote.model.Task
import com.example.tp_loomo.data.repository.ProjectRepository
import kotlinx.coroutines.launch

class TaskDetailsViewModel : ViewModel() {
    private val repository = ProjectRepository()

    var task by mutableStateOf<Task?>(null)
        private set

    var project by mutableStateOf<Project?>(null)
        private set

    var isLoading by mutableStateOf(true)
        private set

    fun loadTaskDetails(taskId: Int) {
        viewModelScope.launch {
            isLoading = true
            try {
                // 1. Vai buscar a Tarefa
                val loadedTask = repository.getTaskById(taskId)
                task = loadedTask

                // 2. Se a tarefa existir, vai buscar o nome do Projeto associado
                if (loadedTask != null) {
                    project = repository.getProjectById(loadedTask.project_id)
                }
            } catch (e: Exception) {
                task = null
                project = null
            } finally {
                isLoading = false
            }
        }
    }

    // Função para o botão "Marcar como Concluído"
    fun completeTask(taskId: Int, onSuccess: () -> Unit) {
        // Aqui no futuro podes fazer o update para "completed" no Supabase
        onSuccess()
    }
}