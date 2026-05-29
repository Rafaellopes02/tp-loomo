package com.example.tp_loomo.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tp_loomo.data.remote.model.Project
import com.example.tp_loomo.data.remote.model.UserProfile
import com.example.tp_loomo.data.repository.ProjectRepository
import kotlinx.coroutines.launch

class ProjectDetailsViewModel : ViewModel() {
    private val repository = ProjectRepository()

    var project by mutableStateOf<Project?>(null)
        private set

    var teamMembers by mutableStateOf<List<UserProfile>>(emptyList())
        private set

    var isLoading by mutableStateOf(true)
        private set

    fun loadProjectDetails(projectId: Int) {
        viewModelScope.launch {
            isLoading = true
            try {
                project = repository.getProjectById(projectId)
                teamMembers = repository.getProjectMembers(projectId)
            } catch (e: Exception) {
                android.util.Log.e("ERRO_LOOMO", "Falha ao carregar projeto $projectId: ${e.message}", e)
                project = null
                teamMembers = emptyList()
            } finally {
                isLoading = false
            }
        }
    }

    fun deleteProject(id: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val success = repository.deleteProject(id)
            if (success) {
                onSuccess()
            }
        }
    }

    fun updateProject(id: Int, newName: String, newDescription: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            val success = repository.updateProject(id, newName, newDescription)
            if (success) {
                project = project?.copy(name = newName, description = newDescription)
                onSuccess()
            }
            isLoading = false
        }
    }
}