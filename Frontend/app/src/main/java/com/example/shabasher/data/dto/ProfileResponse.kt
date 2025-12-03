package com.example.shabasher.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProfileResponse(
    val id: String,
    val name: String,
    val email: String,
    val createdAt: String
)