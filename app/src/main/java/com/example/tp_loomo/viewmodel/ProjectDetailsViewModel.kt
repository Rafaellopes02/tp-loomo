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
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

// --- MODELOS PARA INSERIR TAREFAS NA BD ---
@Serializable
data class TaskInsert(
    val project_id: Int,
    val title: String,
    val description: String,
    val due_date: String?
)

@Serializable
data class TaskAssignmentInsert(
    val task_id: Int,
    val user_id: String
)

class ProjectDetailsViewModel : ViewModel() {
    private val repository = ProjectRepository()

    var project by mutableStateOf<Project?>(null)
        private set

    var teamMembers by mutableStateOf<List<UserProfile>>(emptyList())
        private set

    var projectTasks by mutableStateOf<List<Task>>(emptyList())
        private set

    var isLoading by mutableStateOf(true)
        private set

    var allUsers by mutableStateOf<List<UserProfile>>(emptyList())
        private set

    fun loadProjectDetails(projectId: Int) {
        viewModelScope.launch {
            isLoading = true
            try {
                project = repository.getProjectById(projectId)
                teamMembers = repository.getProjectMembers(projectId)
                projectTasks = repository.getProjectTasks(projectId)
                allUsers = repository.getAllRegularUsers()
            } catch (e: Exception) {
                android.util.Log.e("ERRO_LOOMO", "Falha ao carregar projeto $projectId: ${e.message}", e)
                project = null
                teamMembers = emptyList()
                projectTasks = emptyList()
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

    fun createTask(projectId: Int, title: String, description: String, dueDate: String?, selectedMemberIds: List<String>, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val newTask = TaskInsert(
                    project_id = projectId,
                    title = title,
                    description = description,
                    due_date = dueDate
                )

                // 1. Insere a tarefa e pede a linha de volta para saber qual é o ID gerado
                val insertedTask = supabase.postgrest["tasks"]
                    .insert(newTask) { select() }
                    .decodeSingle<Task>()

                // 2. Se o utilizador escolheu membros, grava TODOS na tabela task_assignments
                if (selectedMemberIds.isNotEmpty() && insertedTask.id != null) {
                    val assignments = selectedMemberIds.map { userId ->
                        TaskAssignmentInsert(
                            task_id = insertedTask.id,
                            user_id = userId
                        )
                    }
                    // O Supabase permite inserir uma lista inteira de uma só vez!
                    supabase.postgrest["task_assignments"].insert(assignments)
                }

                // 3. Atualiza os dados do ecrã e fecha o modal
                loadProjectDetails(projectId)
                onSuccess()
            } catch (e: Exception) {
                android.util.Log.e("ProjectDetailsVM", "Erro ao criar tarefa: ${e.message}")
            }
        }
    }
}