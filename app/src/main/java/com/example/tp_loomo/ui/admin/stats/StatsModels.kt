package com.example.tp_loomo.ui.admin.stats

import kotlinx.serialization.Serializable

@Serializable
data class StatProject(
    val id: Int,
    val name: String,
    val project_manager_id: String? = null,
    val description: String? = null
)

@Serializable
data class StatTask(
    val id: Int,
    val project_id: Int,
    val title: String,
    val description: String? = null,
    val due_date: String? = null,
    val location: String? = null,
    val status: String? = null,
    val completion_rate: Int? = null,
    val estimated_time: Int? = null,
    val actual_time: Int? = null,
    val notes: String? = null
)

@Serializable
data class StatTaskRecord(
    val id: Int? = null,
    val task_id: Int,
    val user_id: String
)

@Serializable
data class StatTaskAssignment(
    val id: Int? = null,
    val task_id: Int,
    val user_id: String
)

@Serializable
data class StatUser(
    val id: String,
    val full_name: String? = null,
    val role: String? = null,
    val username: String? = null,
    val email: String? = null,
    val avatar_url: String? = null
)

@Serializable
data class StatProjectMember(val project_id: Int, val user_id: String)