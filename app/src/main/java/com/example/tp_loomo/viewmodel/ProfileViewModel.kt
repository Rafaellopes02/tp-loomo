package com.example.tp_loomo.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tp_loomo.data.repository.ProfileRepository
import com.example.tp_loomo.data.repository.UserProfileData
import kotlinx.coroutines.launch
import android.app.Application

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ProfileRepository(application.applicationContext)

    var userData by mutableStateOf<UserProfileData?>(null)
        private set

    init {
        loadProfile() // ← carrega automaticamente ao criar o ViewModel
    }

    fun loadProfile() {
        viewModelScope.launch {
            userData = repository.getUserData()
        }
    }
    fun logout(onLogoutSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.signOut()
            onLogoutSuccess()
        }
    }
}