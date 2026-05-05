package com.example.tp_loomo.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String,
    val username: String? = null,
    val full_name: String? = null,
    val avatar_url: String? = null,
    val role: String? = null,
    val email: String? = null
)