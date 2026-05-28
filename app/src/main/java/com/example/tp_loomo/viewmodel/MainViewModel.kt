package com.example.tp_loomo.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tp_loomo.data.repository.MainRepository
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val repository = MainRepository()

    var currentRole by mutableStateOf("loading")
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun fetchUserRole() {
        viewModelScope.launch {
            try {
                currentRole = repository.getUserRole()
            } catch (e: Exception) {
                currentRole = "user"
                errorMessage = e.message
            }
        }
    }
}