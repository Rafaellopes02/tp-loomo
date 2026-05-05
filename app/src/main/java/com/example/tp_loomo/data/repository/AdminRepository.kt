package com.example.tp_loomo.data.repository

import com.example.tp_loomo.data.remote.api.supabase
import com.example.tp_loomo.data.remote.model.UserProfile
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.JsonObject

class AdminRepository {

    // Buscar utilizadores por cargo para os seletores
    suspend fun getUsersByRole(role: String): List<UserProfile> {
        return supabase.postgrest["profiles"].select {
            filter { eq("role", role) }
        }.decodeList<UserProfile>()
    }

    // Criar novo utilizador (Auth + Profile)
    suspend fun createNewUser(email: String, fullName: String, username: String, role: String) {
        supabase.auth.signUpWith(Email) {
            this.email = email
            this.password = "loomo26"
            data = buildJsonObject {
                put("full_name", fullName)
                put("username", username)
                put("role", role)
            }
        }
    }

    // Criar projeto e associar equipa
    suspend fun createProject(
        name: String,
        desc: String,
        managerId: String,
        startDate: String,
        endDate: String?,
        teamMemberIds: List<String>
    ): JsonObject {
        val projectData = buildJsonObject {
            put("name", name)
            put("description", desc)
            put("start_date", startDate)
            if (endDate != null) put("end_date", endDate)
            put("project_manager_id", managerId)
            put("status", "active")
        }

        val response = supabase.postgrest["projects"].insert(projectData) { select() }.decodeSingle<JsonObject>()
        val projectId = response["id"]?.toString()?.toIntOrNull()

        if (projectId != null && teamMemberIds.isNotEmpty()) {
            val membersData = teamMemberIds.map { userId ->
                buildJsonObject {
                    put("project_id", projectId)
                    put("user_id", userId)
                }
            }
            supabase.postgrest["project_members"].insert(membersData)
        }
        return response
    }
}