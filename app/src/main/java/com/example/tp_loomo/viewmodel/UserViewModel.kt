package com.example.tp_loomo.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tp_loomo.data.remote.model.Project
import com.example.tp_loomo.data.repository.ProjectRepository
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {
    private val repository = ProjectRepository()

    var userProjects by mutableStateOf<List<Project>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    fun loadDashboardData() {
        viewModelScope.launch {
            isLoading = true
            try {
                userProjects = repository.getMemberProjects()
            } catch (e: Exception) {
                userProjects = emptyList()
            } finally {
                isLoading = false
            }
        }
    }
}