package com.example.tp_loomo.data.repository

import com.example.tp_loomo.data.remote.api.supabase
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.Serializable

@Serializable
data class UserRole(val role: String)

class MainRepository {
    suspend fun getUserRole(): String {
        val userId = supabase.auth.currentUserOrNull()?.id ?: return "user"
        return try {
            val profile = supabase.postgrest["profiles"]
                .select(columns = Columns.list("role")) { filter { eq("id", userId) } }
                .decodeSingle<UserRole>()
            profile.role.trim().lowercase()
        } catch (e: Exception) {
            "user"
        }
    }
}