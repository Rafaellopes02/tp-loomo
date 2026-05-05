package com.example.tp_loomo.data.repository

import com.example.tp_loomo.data.remote.api.supabase
import com.example.tp_loomo.data.remote.model.Project
import com.example.tp_loomo.data.remote.model.UserProfile
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest

class ProjectRepository {

    // Lista projetos onde o utilizador é o Manager
    suspend fun getManagedProjects(): List<Project> {
        val userId = supabase.auth.currentUserOrNull()?.id ?: return emptyList()
        return supabase.postgrest["projects"].select {
            filter { eq("project_manager_id", userId) }
        }.decodeList<Project>()
    }

    // Lista projetos onde o utilizador é apenas membro da equipa
    suspend fun getMemberProjects(): List<Project> {
        val userId = supabase.auth.currentUserOrNull()?.id ?: return emptyList()

        // Primeiro buscamos os IDs dos projetos na tabela de membros
        val projectIds = supabase.postgrest["project_members"].select {
            filter { eq("user_id", userId) }
        }.decodeList<Map<String, Int>>().mapNotNull { it["project_id"] }

        if (projectIds.isEmpty()) return emptyList()

        return supabase.postgrest["projects"].select {
            // CORREÇÃO: Forçamos cada item da lista a ser interpretado como "Any" para o Supabase
            filter { isIn("id", projectIds.map { it as Any }) }
        }.decodeList<Project>()
    }

    // Busca os membros (avatares) de um projeto específico
    suspend fun getProjectMembers(projectId: Int): List<UserProfile> {
        val memberIds = supabase.postgrest["project_members"].select {
            filter { eq("project_id", projectId) }
        }.decodeList<Map<String, String>>().mapNotNull { it["user_id"] }

        // Proteção extra caso não existam membros
        if (memberIds.isEmpty()) return emptyList()

        return supabase.postgrest["profiles"].select {
            // CORREÇÃO: Forçamos a lista a Any
            filter { isIn("id", memberIds.map { it as Any }) }
        }.decodeList<UserProfile>()
    }
    // Busca um projeto específico pelo seu ID
    suspend fun getProjectById(projectId: Int): Project? {
        return try {
            supabase.postgrest["projects"].select {
                filter { eq("id", projectId) }
            }.decodeSingleOrNull<Project>()
        } catch (e: Exception) {
            null
        }
    }
}