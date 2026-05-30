package com.example.tp_loomo.data.repository

import com.example.tp_loomo.data.remote.api.supabase
import com.example.tp_loomo.data.remote.model.Task
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class TaskRepository {

    // Linha da tabela ponte task_assignments (utilizador <-> tarefa)
    @Serializable
    data class TaskAssignmentRow(val task_id: Int, val user_id: String)

    // RF12 — Buscar todas as tarefas de um projeto (vista do Gestor dentro do projeto)
    suspend fun getTasksByProject(projectId: Int): List<Task> {
        return try {
            supabase.postgrest["tasks"].select {
                filter { eq("project_id", projectId) }
            }.decodeList<Task>().sortedBy { it.id }
        } catch (e: Exception) {
            android.util.Log.e("TASK_REPO", "Erro getTasksByProject: ${e.message}", e)
            emptyList()
        }
    }

    // RF15 — Tarefas pendentes do utilizador autenticado (deixa pronto para o Tiago/Rafael)
    suspend fun getMyPendingTasks(): List<Task> {
        return getMyTasksByStatus(listOf("pending", "in_progress"))
    }

    // RF16 — Histórico de tarefas concluídas do utilizador autenticado
    suspend fun getMyCompletedTasks(): List<Task> {
        return getMyTasksByStatus(listOf("completed"))
    }

    private suspend fun getMyTasksByStatus(statuses: List<String>): List<Task> {
        val userId = supabase.auth.currentUserOrNull()?.id ?: return emptyList()
        return try {
            // 1) IDs das tarefas atribuídas a este utilizador
            val taskIds = supabase.postgrest["task_assignments"].select {
                filter { eq("user_id", userId) }
            }.decodeList<TaskAssignmentRow>().map { it.task_id }

            if (taskIds.isEmpty()) return emptyList()

            // 2) Tarefas com esses IDs e nos estados pedidos
            supabase.postgrest["tasks"].select {
                filter {
                    isIn("id", taskIds.map { it as Any })
                    isIn("status", statuses.map { it as Any })
                }
            }.decodeList<Task>().sortedByDescending { it.updated_at ?: "" }
        } catch (e: Exception) {
            android.util.Log.e("TASK_REPO", "Erro getMyTasksByStatus: ${e.message}", e)
            emptyList()
        }
    }

    // RF10 — Criar nova tarefa associada a um projeto
    suspend fun createTask(
        projectId: Int,
        title: String,
        description: String?,
        dueDate: String?
    ): Boolean {
        return try {
            val taskData = buildJsonObject {
                put("project_id", projectId)
                put("title", title)
                if (!description.isNullOrBlank()) put("description", description)
                if (!dueDate.isNullOrBlank()) put("due_date", dueDate)
                put("status", "pending")
            }
            supabase.postgrest["tasks"].insert(taskData)
            true
        } catch (e: Exception) {
            android.util.Log.e("TASK_REPO", "Erro createTask: ${e.message}", e)
            false
        }
    }

    // RF19 — Marcar tarefa como concluída
    suspend fun markAsCompleted(taskId: Int): Boolean {
        return try {
            val result = supabase.postgrest["tasks"].update(
                {
                    set("status", "completed")
                    set("completion_rate", 100)
                }
            ) {
                filter { eq("id", taskId) }
                select()
            }.decodeList<Task>()
            if (result.isEmpty()) false else true
        } catch (e: Exception) {
            android.util.Log.e("TASK_REPO", "Erro markAsCompleted: ${e.message}", e)
            false
        }
    }
}