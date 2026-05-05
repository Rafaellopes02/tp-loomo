package com.example.tp_loomo.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tp_loomo.data.repository.ProfileRepository
import com.example.tp_loomo.data.repository.UserProfileData
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    private val repository = ProfileRepository()

    var userData by mutableStateOf<UserProfileData?>(null)
        private set

    fun loadProfile() {
        userData = repository.getUserData()
    }

    fun logout(onLogoutSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.signOut()
            onLogoutSuccess()
        }
    }
}