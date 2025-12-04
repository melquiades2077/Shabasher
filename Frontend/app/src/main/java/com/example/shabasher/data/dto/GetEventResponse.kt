package com.example.shabasher.data.dto

import kotlinx.serialization.Serializable

// Получение события по ID
@Serializable
data class GetEventResponse(
    val id: String,
    val title: String,
    val description: String,
    val address: String,
    val dateTime: String,
    val creatorId: String,
    val createdAt: String,
    val participants: List<EventParticipantDto> = emptyList()
)