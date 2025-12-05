package com.example.shabasher.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class EventParticipantDto(
    val user: UserResponse,
    val status: String,
    val role: String? = null
)