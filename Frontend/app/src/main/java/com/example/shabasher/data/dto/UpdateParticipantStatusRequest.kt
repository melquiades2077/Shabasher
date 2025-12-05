package com.example.shabasher.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdateParticipantStatusRequest(
    val eventId: String,
    val status: String
)
