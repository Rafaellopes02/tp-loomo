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

data class TaskUiModel(
    val task: Task,
    val project: Project?,
    val avatars: List<String?>
)

class TasksUserViewModel : ViewModel() {
    private val repository = ProjectRepository()

    var tasksList by mutableStateOf<List<TaskUiModel>>(emptyList())
        private set

    var isLoading by mutableStateOf(true)
        private set

    fun loadTasks() {
        viewModelScope.launch {
            isLoading = true
            try {
                val userId = supabase.auth.currentUserOrNull()?.id ?: return@launch

                // Vai buscar as tarefas deste user
                val rawTasks = repository.getUserTasks(userId)

                val combinedList = mutableListOf<TaskUiModel>()

                // Para cada tarefa, vai buscar o projeto e os membros
                for (task in rawTasks) {
                    val project = repository.getProjectById(task.project_id)
                    val assignees = task.id?.let { repository.getTaskAssignees(it) } ?: emptyList()
                    val avatars = assignees.map { it.avatar_url }

                    combinedList.add(TaskUiModel(task, project, avatars))
                }

                tasksList = combinedList

            } catch (e: Exception) {
                android.util.Log.e("TasksUserVM", "Erro ao carregar ecrã de tarefas: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }
}