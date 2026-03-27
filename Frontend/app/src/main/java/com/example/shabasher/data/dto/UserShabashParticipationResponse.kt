package com.example.shabasher.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserShabashParticipationResponse(
    val shabashId: String,
    val shabashName: String,
    val status: String
)