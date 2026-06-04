package com.example.tp_loomo.data.repository

import android.util.Log
import com.example.tp_loomo.data.remote.api.supabase
import com.example.tp_loomo.data.remote.model.Project
import com.example.tp_loomo.data.remote.model.Task
import com.example.tp_loomo.data.remote.model.UserProfile
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.Serializable

class ProjectRepository {

    @Serializable
    data class ProjectMemberRow(val project_id: Int, val user_id: String)

    @Serializable
    data class TaskAssignmentRow(val task_id: Int, val user_id: String)

    @Serializable
    data class ProjectUpdate(val name: String, val description: String?)

    @Serializable
    data class TaskRecordRow(
        val id: Int? = null,
        val task_id: Int,
        val user_id: String,
        val progress: Int,
        val location: String,
        val date: String,
        val time_spent: String,
        val observations: String,
        val photo_url: String? = null,
        val created_at: String? = null
    )

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
            Log.e("REPO_ERRO", "Erro a carregar tarefas: ${e.message}")
            emptyList()
        }
    }

    suspend fun getTaskById(taskId: Int): Task? {
        return try {
            supabase.postgrest["tasks"].select {
                filter { eq("id", taskId) }
            }.decodeSingleOrNull<Task>()
        } catch (e: Exception) {
            Log.e("REPO_ERRO", "Erro a carregar tarefa: ${e.message}")
            null
        }
    }

    suspend fun getAllRegularUsers(): List<UserProfile> {
        return try {
            supabase.postgrest["profiles"].select {
                filter {
                    // Assume que a tua coluna se chama "role" e o valor é "user"
                    eq("role", "user")
                }
            }.decodeList<UserProfile>()
        } catch (e: Exception) {
            Log.e("REPO_ERRO", "Erro a carregar utilizadores: ${e.message}")
            emptyList()
        }
    }
    suspend fun getUserTasks(userId: String): List<Task> {
        return try {
            Log.d("ProjectRepository", "🔍 A procurar tarefas para o user: $userId")

            // 1. Vai buscar APENAS as colunas task_id e user_id
            val assignments = supabase.postgrest["task_assignments"].select(
                columns = Columns.list("task_id", "user_id")
            ) {
                filter {
                    eq("user_id", userId)
                }
            }.decodeList<TaskAssignmentRow>()

            Log.d("ProjectRepository", "✅ Atribuições encontradas: ${assignments.size}")

            // Se ele não tiver tarefas atribuídas, devolvemos logo uma lista vazia
            if (assignments.isEmpty()) {
                return emptyList()
            }

            // 2. Extrai apenas os IDs das tarefas (ex: [17, 18, 20])
            val taskIds = assignments.map { it.task_id }
            Log.d("ProjectRepository", "📌 IDs das tarefas dele: $taskIds")

            // 3. Vai à tabela 'tasks' buscar apenas as tarefas que têm estes IDs
            val tasks = supabase.postgrest["tasks"].select {
                filter {
                    isIn("id", taskIds.map { it as Any })
                }
            }.decodeList<Task>()

            Log.d("ProjectRepository", "🚀 Tarefas finais descarregadas: ${tasks.size}")
            tasks

        } catch (e: Exception) {
            Log.e("ProjectRepository", "❌ ERRO GRAVE ao carregar tarefas: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun isUserAssignedToTask(taskId: Int, userId: String): Boolean {
        return try {
            val assignments = supabase.postgrest["task_assignments"].select(
                columns = Columns.list("task_id", "user_id")
            ) {
                filter {
                    eq("task_id", taskId)
                    eq("user_id", userId)
                }
            }.decodeList<TaskAssignmentRow>()

            assignments.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getTaskAssignees(taskId: Int): List<UserProfile> {
        return try {
            val assignments = supabase.postgrest["task_assignments"].select(
                columns = Columns.list("user_id")
            ) {
                filter { eq("task_id", taskId) }
            }.decodeList<TaskAssignmentRow>()

            if (assignments.isEmpty()) return emptyList()

            val userIds = assignments.map { it.user_id }
            supabase.postgrest["profiles"].select {
                filter { isIn("id", userIds.map { it as Any }) }
            }.decodeList<UserProfile>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getTaskRecords(taskId: Int): List<TaskRecordRow> {
        return try {
            supabase.postgrest["task_records"].select {
                filter { eq("task_id", taskId) }
            }.decodeList<TaskRecordRow>()
        } catch (e: Exception) {
            android.util.Log.e("ProjectRepository", "Erro ao carregar registos: ${e.message}")
            emptyList()
        }
    }
}