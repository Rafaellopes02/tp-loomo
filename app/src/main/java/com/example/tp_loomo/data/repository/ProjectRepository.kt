package com.example.tp_loomo.data.repository

import com.example.tp_loomo.data.remote.api.supabase
import com.example.tp_loomo.data.remote.model.Project
import com.example.tp_loomo.data.remote.model.Task
import com.example.tp_loomo.data.remote.model.UserProfile
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.Serializable

class ProjectRepository {

    @Serializable
    data class ProjectMemberRow(val project_id: Int, val user_id: String)

    suspend fun getManagedProjects(): List<Project> {
        val userId = supabase.auth.currentUserOrNull()?.id ?: return emptyList()
        return supabase.postgrest["projects"].select {
            filter { eq("project_manager_id", userId) }
        }.decodeList<Project>()
    }

    suspend fun getMemberProjects(): List<Project> {
        val userId = supabase.auth.currentUserOrNull()?.id ?: return emptyList()

        val projectIds = supabase.postgrest["project_members"].select {
            filter { eq("user_id", userId) }
        }.decodeList<ProjectMemberRow>().map { it.project_id }

        if (projectIds.isEmpty()) return emptyList()

        return supabase.postgrest["projects"].select {
            filter { isIn("id", projectIds.map { it as Any }) }
        }.decodeList<Project>()
    }

    suspend fun getProjectMembers(projectId: Int): List<UserProfile> {
        val memberIds = supabase.postgrest["project_members"].select {
            filter { eq("project_id", projectId) }
        }.decodeList<ProjectMemberRow>().map { it.user_id }

        if (memberIds.isEmpty()) return emptyList()

        return supabase.postgrest["profiles"].select {
            filter { isIn("id", memberIds.map { it as Any }) }
        }.decodeList<UserProfile>()
    }

    suspend fun getProjectById(projectId: Int): Project? {
        return try {
            supabase.postgrest["projects"].select {
                filter { eq("id", projectId) }
            }.decodeSingleOrNull<Project>()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getAllProjects(): List<Project> {
        return try {
            supabase.postgrest["projects"]
                .select()
                .decodeList<Project>()
                .sortedByDescending { it.id }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun deleteProject(id: Int): Boolean {
        return try {
            supabase.postgrest["projects"].delete {
                filter {
                    eq("id", id)
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    @Serializable
    data class ProjectUpdate(val name: String, val description: String?)

    suspend fun updateProject(id: Int, name: String, description: String?): Boolean {
        return try {
            supabase.postgrest["projects"].update(ProjectUpdate(name, description)) {
                filter { eq("id", id) }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getProjectTasks(projectId: Int): List<Task> {
        return try {
            supabase.postgrest["tasks"].select {
                filter { eq("project_id", projectId) }
            }.decodeList<Task>()
        } catch (e: Exception) {
            android.util.Log.e("REPO_ERRO", "Erro a carregar tarefas: ${e.message}")
            emptyList()
        }
    }

    suspend fun getTaskById(taskId: Int): Task? {
        return try {
            supabase.postgrest["tasks"].select {
                filter { eq("id", taskId) }
            }.decodeSingleOrNull<Task>()
        } catch (e: Exception) {
            android.util.Log.e("REPO_ERRO", "Erro a carregar tarefa: ${e.message}")
            null
        }
    }

    suspend fun getAllRegularUsers(): List<UserProfile> {
        return try {
            supabase.postgrest["profiles"].select {
                filter {
                    // Assume que a tua coluna se chama "role" e o valor é "user"
                    // (Se na tua base de dados a coluna se chamar, por exemplo, "tipo", altera aqui!)
                    eq("role", "user")
                }
            }.decodeList<UserProfile>()
        } catch (e: Exception) {
            android.util.Log.e("REPO_ERRO", "Erro a carregar utilizadores: ${e.message}")
            emptyList()
        }
    }
}