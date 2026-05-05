package com.example.tp_loomo.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class Task(
    val id: Int? = null,
    val project_id: Int,
    val title: String,
    val status: String = "pending",
    val due_date: String? = null
)