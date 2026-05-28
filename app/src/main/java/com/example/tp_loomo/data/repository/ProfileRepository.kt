package com.example.tp_loomo.data.repository

import com.example.tp_loomo.data.remote.api.supabase
import io.github.jan.supabase.gotrue.auth

data class UserProfileData(
    val nomeCompleto: String,
    val nomeUtilizador: String,
    val avatarUrl: String?
)

class ProfileRepository {
    fun getUserData(): UserProfileData? {
        val user = supabase.auth.currentUserOrNull() ?: return null
        return UserProfileData(
            nomeCompleto = user.userMetadata?.get("full_name").toString().replace("\"", ""),
            nomeUtilizador = "@" + user.userMetadata?.get("username").toString().replace("\"", ""),
            avatarUrl = user.userMetadata?.get("avatar_url")?.toString()?.replace("\"", "")
        )
    }

    suspend fun signOut() {
        supabase.auth.signOut()
    }
}