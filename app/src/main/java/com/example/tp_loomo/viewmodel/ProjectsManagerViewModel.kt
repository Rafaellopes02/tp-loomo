package com.example.tp_loomo.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tp_loomo.data.remote.api.supabase
import com.example.tp_loomo.data.remote.model.Project
import com.example.tp_loomo.data.repository.ProjectRepository
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch

// Um modelo para agrupar tudo o que o cartão precisa!
data class ProjectUiModel(
    val project: Project,
    val avatars: List<String?>,
    val progress: Int,
    val pendingTasks: Int,
    val completedTasks: Int
)

class ProjectsManagerViewModel : ViewModel() {
    private val repository = ProjectRepository()

    var projectsList by mutableStateOf<List<ProjectUiModel>>(emptyList())
        private set

    var isLoading by mutableStateOf(true)
        private set

    fun loadProjects() {
        viewModelScope.launch {
            isLoading = true
            try {
                val userId = supabase.auth.currentUserOrNull()?.id ?: return@launch

                // 1. Carrega os projetos em que ele é Gestor
                val managedProjects = repository.getManagedProjects()

                val combinedList = mutableListOf<ProjectUiModel>()

                // 2. Para cada projeto, faz as contas das tarefas e dos avatares
                for (project in managedProjects) {
                    val projectId = project.id ?: continue

                    val members = repository.getProjectMembers(projectId)
                    val avatars = members.map { it.avatar_url }

                    val tasks = repository.getProjectTasks(projectId)

                    val totalTasks = tasks.size
                    // Consideramos concluídas as tarefas com status 'completed' ou 100% de progresso
                    val completedTasks = tasks.count { it.status == "completed" || it.completion_rate == 100 }
                    val pendingTasks = totalTasks - completedTasks

                    val progress = if (totalTasks > 0) {
                        ((completedTasks.toFloat() / totalTasks) * 100).toInt()
                    } else {
                        0
                    }

                    combinedList.add(ProjectUiModel(project, avatars, progress, pendingTasks, completedTasks))
                }

                projectsList = combinedList

            } catch (e: Exception) {
                android.util.Log.e("ProjectsManagerVM", "Erro ao carregar projetos: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }
}