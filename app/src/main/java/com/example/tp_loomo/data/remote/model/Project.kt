package com.example.tp_loomo.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class Project(
    val id: Int,
    val name: String,
    val description: String? = null,
    val start_date: String? = null,
    val end_date: String? = null,
    val project_manager_id: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null,
    val status: String? = null,
    val cover_url: String? = null
)