package com.example.tp_loomo.data.repository

import android.content.Context
import com.example.tp_loomo.data.remote.api.supabase
import com.example.tp_loomo.data.remote.model.UserProfile
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

data class UserProfileData(
    val nomeCompleto: String,
    val nomeUtilizador: String,
    val avatarUrl: String?
)

class ProfileRepository(private val context: Context) {

    private val prefs get() = context.getSharedPreferences("offline_prefs", Context.MODE_PRIVATE)

    suspend fun getUserData(): UserProfileData? {
        val userId = supabase.auth.currentUserOrNull()?.id ?: return null

        return try {
            // Busca sempre da tabela profiles (fonte de verdade)
            val result = supabase.postgrest["profiles"]
                .select(columns = Columns.list("full_name", "username", "avatar_url")) {
                    filter { eq("id", userId) }
                }
                .decodeSingle<UserProfile>()

            val name     = result.full_name ?: ""
            val username = result.username ?: ""
            val avatar   = result.avatar_url

            // Atualiza cache local
            prefs.edit()
                .putString("cached_full_name", name)
                .putString("cached_username", username)
                .putString("cached_avatar_url", avatar)
                .putBoolean("has_pending_profile_update", false)
                .apply()

            UserProfileData(
                nomeCompleto   = name,
                nomeUtilizador = "@$username",
                avatarUrl      = avatar
            )

        } catch (e: Exception) {
            // Sem rede — usa cache local
            val cachedName     = prefs.getString("cached_full_name", null)
            val cachedUsername = prefs.getString("cached_username", null)

            if (cachedName != null) {
                UserProfileData(
                    nomeCompleto   = cachedName,
                    nomeUtilizador = "@${cachedUsername ?: ""}",
                    avatarUrl      = prefs.getString("cached_avatar_url", null)
                )
            } else {
                // Fallback para auth metadata se não há cache nenhum
                val user = supabase.auth.currentUserOrNull()
                UserProfileData(
                    nomeCompleto   = user?.userMetadata?.get("full_name")?.toString()?.replace("\"", "") ?: "",
                    nomeUtilizador = "@${user?.userMetadata?.get("username")?.toString()?.replace("\"", "") ?: ""}",
                    avatarUrl      = user?.userMetadata?.get("avatar_url")?.toString()?.replace("\"", "")
                )
            }
        }
    }

    fun savePendingUpdate(fullName: String, username: String, avatarUrl: String?) {
        prefs.edit()
            .putBoolean("has_pending_profile_update", true)
            .putString("pending_full_name", fullName)
            .putString("pending_username", username)
            .putString("pending_avatar_url", avatarUrl)
            .apply()
    }

    suspend fun syncPendingUpdate(): Boolean {
        val hasPending = prefs.getBoolean("has_pending_profile_update", false)
        if (!hasPending) return true

        val userId = supabase.auth.currentUserOrNull()?.id ?: return false

        val fullName  = prefs.getString("pending_full_name", null) ?: return false
        val username  = prefs.getString("pending_username", null) ?: return false
        val avatarUrl = prefs.getString("pending_avatar_url", null)

        return try {
            val updateData = buildJsonObject {
                put("full_name", fullName)
                put("username", username)
                if (avatarUrl != null) put("avatar_url", avatarUrl)
            }

            supabase.postgrest["profiles"].update(updateData) {
                filter { eq("id", userId) }
            }

            // Atualiza cache com os dados sincronizados
            prefs.edit()
                .putBoolean("has_pending_profile_update", false)
                .putString("cached_full_name", fullName)
                .putString("cached_username", username)
                .putString("cached_avatar_url", avatarUrl)
                .remove("pending_full_name")
                .remove("pending_username")
                .remove("pending_avatar_url")
                .apply()

            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun signOut() {
        supabase.auth.signOut()
    }
}