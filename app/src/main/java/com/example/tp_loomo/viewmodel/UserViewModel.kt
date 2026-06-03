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

class UserViewModel : ViewModel() {
    private val repository = ProjectRepository()

    var currentUser by mutableStateOf<UserProfile?>(null)
        private set

    var userProjects by mutableStateOf<List<Project>>(emptyList())
        private set

    var userTasks by mutableStateOf<List<Task>>(emptyList())
        private set

    var isLoading by mutableStateOf(true)
        private set

    fun loadUserDashboard() {
        viewModelScope.launch {
            isLoading = true
            try {
                val userId = supabase.auth.currentUserOrNull()?.id
                if (userId != null) {
                    // 1. Carregar perfil do utilizador
                    currentUser = supabase.postgrest["profiles"]
                        .select { filter { eq("id", userId) } }
                        .decodeSingleOrNull<UserProfile>()

                    // 2. Carregar projetos onde o utilizador é membro
                    userProjects = repository.getMemberProjects()

                    // 3. Carregar APENAS as tarefas atribuídas a ele (AQUI ESTÁ A MAGIA!)
                    userTasks = repository.getUserTasks(userId)
                }
            } catch (e: Exception) {
                android.util.Log.e("UserVM", "Erro ao carregar dashboard: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }
}