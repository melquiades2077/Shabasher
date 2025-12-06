package com.example.shabasher.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ShabashParticipantDto(
    val user: UserResponse,
    val status: String,
    val role: String? = null
)