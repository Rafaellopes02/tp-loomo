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
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class ManagerAvatarRow(val avatar_url: String? = null)

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

                // Carrega os projetos em que ele é Gestor
                val managedProjects = repository.getManagedProjects()

                val combinedList = mutableListOf<ProjectUiModel>()

                // Para cada projeto, faz as contas das tarefas e dos avatares
                for (project in managedProjects) {
                    val projectId = project.id ?: continue

                    var managerAvatar: String? = null
                    if (project.project_manager_id != null) {
                        try {
                            val profile = supabase.postgrest["profiles"]
                                .select(columns = Columns.list("avatar_url")) {
                                    filter { eq("id", project.project_manager_id) }
                                }.decodeSingleOrNull<ManagerAvatarRow>()
                            managerAvatar = profile?.avatar_url
                        } catch (e: Exception) {
                            // Ignora silenciosamente se o perfil do gestor for apagado ou não tiver avatar
                        }
                    }

                    val members = repository.getProjectMembers(projectId)
                    val teamAvatars = members.map { it.avatar_url }

                    val combinedAvatars = mutableListOf<String?>()
                    combinedAvatars.add(managerAvatar)
                    combinedAvatars.addAll(teamAvatars)
                    val finalAvatars = combinedAvatars.distinct()
                    val tasks = repository.getProjectTasks(projectId)

                    val totalTasks = tasks.size
                    val completedTasks = tasks.count {
                        it.status?.lowercase() in listOf("completed", "concluded", "concluída", "concluido") || it.completion_rate == 100
                    }
                    val pendingTasks = totalTasks - completedTasks

                    val progress = if (totalTasks > 0) {
                        ((completedTasks.toFloat() / totalTasks) * 100).toInt()
                    } else {
                        0
                    }

                    combinedList.add(ProjectUiModel(project, finalAvatars, progress, pendingTasks, completedTasks))
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