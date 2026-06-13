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
import kotlinx.serialization.Serializable

data class TaskRecordUiModel(
    val record: ProjectRepository.TaskRecordRow,
    val userProfile: UserProfile?
)

@Serializable
data class TaskStatusUpdate(val status: String, val completion_rate: Int)

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

    var taskRecords by mutableStateOf<List<TaskRecordUiModel>>(emptyList())
        private set

    fun loadTaskDetails(taskId: Int) {
        viewModelScope.launch {
            isLoading = true
            try {
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

                val rawRecords = repository.getTaskRecords(taskId)
                val userIds = rawRecords.map { it.user_id }.distinct()

                val profiles = if (userIds.isNotEmpty()) {
                    supabase.postgrest["profiles"].select {
                        filter { isIn("id", userIds.map { it as Any }) }
                    }.decodeList<UserProfile>()
                } else emptyList()

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

    fun completeTask(taskId: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                supabase.postgrest["tasks"].update(
                    TaskStatusUpdate(status = "completed", completion_rate = 100)
                ) { filter { eq("id", taskId) } }
                onSuccess()
            } catch (e: Exception) {
                android.util.Log.e("TaskDetailsVM", "Erro ao concluir: ${e.message}")
            }
        }
    }
}