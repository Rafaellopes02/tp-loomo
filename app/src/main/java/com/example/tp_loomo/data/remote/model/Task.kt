package com.example.tp_loomo.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class Task(
    val id: Int? = null,
    val project_id: Int,
    val title: String,
    val description: String? = null,
    val due_date: String? = null,
    val location: String? = null,
    val completion_rate: Int? = null,
    val estimated_time: Int? = null,
    val actual_time: Int? = null,
    val notes: String? = null,
    val status: String = "pending",
    val created_at: String? = null,
    val updated_at: String? = null
)