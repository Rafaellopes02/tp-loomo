package com.example.tp_loomo.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tp_loomo.data.remote.api.supabase
import com.example.tp_loomo.data.remote.model.Project
import com.example.tp_loomo.data.remote.model.Task
import com.example.tp_loomo.data.remote.model.UserProfile
import com.example.tp_loomo.data.repository.ProjectRepository
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

data class TaskRecordUiModel(
    val record: ProjectRepository.TaskRecordRow,
    val userProfile: UserProfile?
)

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

    // Nova variável para guardar a lista de registos
    var taskRecords by mutableStateOf<List<TaskRecordUiModel>>(emptyList())
        private set

    fun loadTaskDetails(taskId: Int) {
        viewModelScope.launch {
            isLoading = true
            try {
                // 1. Carrega a tarefa e permissões
                val loadedTask = repository.getTaskById(taskId)
                task = loadedTask

                if (loadedTask != null) {
                    val loadedProject = repository.getProjectById(loadedTask.project_id)
                    project = loadedProject

                    val currentUserId = supabase.auth.currentUserOrNull()?.id
                    if (currentUserId != null) {
                        val isManager = loadedProject?.project_manager_id == currentUserId
                        val isAssigned = repository.isUserAssignedToTask(taskId, currentUserId)
                        isAssignedToCurrentUser = isManager || isAssigned
                    }
                }

                // 2. Carrega os registos de trabalho
                val rawRecords = repository.getTaskRecords(taskId)
                val userIds = rawRecords.map { it.user_id }.distinct()

                // 3. Carrega os perfis dos donos dos registos
                val profiles = if (userIds.isNotEmpty()) {
                    supabase.postgrest["profiles"].select {
                        filter { isIn("id", userIds.map { it as Any }) }
                    }.decodeList<UserProfile>()
                } else emptyList()

                // 4. Junta tudo e ordena do mais recente para o mais antigo
                taskRecords = rawRecords.map { record ->
                    TaskRecordUiModel(
                        record = record,
                        userProfile = profiles.find { it.id == record.user_id }
                    )
                }.sortedByDescending { it.record.created_at }

            } catch (e: Exception) {
                task = null
                project = null
                isAssignedToCurrentUser = false
                taskRecords = emptyList()
            } finally {
                isLoading = false
            }
        }
    }

    // Aproveitei e meti o botão "Concluído" a funcionar de verdade na BD!
    fun completeTask(taskId: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                supabase.postgrest["tasks"].update(
                    mapOf("status" to "completed", "completion_rate" to 100)
                ) { filter { eq("id", taskId) } }
                onSuccess()
            } catch (e: Exception) {
                android.util.Log.e("TaskDetailsVM", "Erro ao concluir: ${e.message}")
            }
        }
    }
}