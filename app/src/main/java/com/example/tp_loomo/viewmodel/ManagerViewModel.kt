package com.example.tp_loomo.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tp_loomo.data.remote.api.supabase
import com.example.tp_loomo.data.remote.model.Project
import com.example.tp_loomo.data.repository.ProjectRepository
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.launch

data class ProjectSummary(
    val project: Project,
    val avatars: List<String?>,
    val pending: Int,
    val completed: Int,
    val progress: Float
)

class ManagerViewModel : ViewModel() {
    private val repository = ProjectRepository()

    var projectSummaries by mutableStateOf<List<ProjectSummary>>(emptyList())
        private set

    var totalPending by mutableStateOf(0)
        private set

    var totalCompleted by mutableStateOf(0)
        private set

    var isLoading by mutableStateOf(false)
        private set

    val managedProjects get() = projectSummaries.map { it.project }

    fun loadDashboardData() {
        viewModelScope.launch {
            isLoading = true
            try {
                val projects = repository.getManagedProjects()

                val summaries = projects.map { project ->
                    val projectId = project.id ?: return@map null

                    // --- 1. BUSCAR AVATAR DO GESTOR ---
                    var managerAvatar: String? = null
                    if (project.project_manager_id != null) {
                        try {
                            // Reutiliza a ManagerAvatarRow que declaraste no ProjectsManagerViewModel
                            val profile = supabase.postgrest["profiles"]
                                .select(columns = Columns.list("avatar_url")) {
                                    filter { eq("id", project.project_manager_id) }
                                }.decodeSingleOrNull<ManagerAvatarRow>()
                            managerAvatar = profile?.avatar_url
                        } catch (e: Exception) {
                            // Ignora se der erro ao buscar perfil do gestor
                        }
                    }

                    // --- 2. BUSCAR TAREFAS E EQUIPA ---
                    val tasks = repository.getProjectTasks(projectId)
                    val members = repository.getProjectMembers(projectId)
                    val teamAvatars = members.map { it.avatar_url }

                    // --- 3. JUNTAR GESTOR + EQUIPA ---
                    val combinedAvatars = mutableListOf<String?>()
                    combinedAvatars.add(managerAvatar)
                    combinedAvatars.addAll(teamAvatars)
                    val finalAvatars = combinedAvatars.distinct()

                    // --- 4. CONTAS DE PROGRESSO ---
                    val completed = tasks.count { it.status == "completed" || it.completion_rate == 100 }
                    val pending = tasks.size - completed
                    val progress = if (tasks.isEmpty()) 0f else completed.toFloat() / tasks.size

                    // Agora injeta a lista combinada de finalAvatars!
                    ProjectSummary(project, finalAvatars, pending, completed, progress)
                }.filterNotNull()

                projectSummaries = summaries
                totalPending = summaries.sumOf { it.pending }
                totalCompleted = summaries.sumOf { it.completed }

            } catch (e: Exception) {
                projectSummaries = emptyList()
                totalPending = 0
                totalCompleted = 0
            } finally {
                isLoading = false
            }
        }
    }
}