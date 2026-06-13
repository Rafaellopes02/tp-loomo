package com.example.tp_loomo.data.repository

import com.example.tp_loomo.data.remote.api.supabase
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Serializable
data class ProfileEmail(val email: String)

class AuthRepository {

    // Função de Login
    suspend fun login(emailOrUsername: String, pass: String) {
        val isEmail = emailOrUsername.contains("@")

        val emailToLogin = if (isEmail) {
            emailOrUsername
        } else {
            val perfis = supabase.postgrest["profiles"]
                .select {
                    filter {
                        eq("username", emailOrUsername)
                    }
                }.decodeList<ProfileEmail>()

            if (perfis.isEmpty()) {
                throw Exception("Username não encontrado.")
            }
            perfis.first().email
        }
        supabase.auth.signInWith(Email) {
            this.email = emailToLogin
            this.password = pass
        }
    }

    // Função de Registo
    suspend fun signUp(fullName: String, username: String, email: String, pass: String, avatarUrl: String?) {
        supabase.auth.signUpWith(Email) {
            this.email = email
            this.password = pass
            this.data = buildJsonObject {
                put("full_name", fullName)
                put("username", username)
                if (avatarUrl != null) put("avatar_url", avatarUrl)
            }
        }
    }
    suspend fun logout() {
        supabase.auth.signOut()
    }
}