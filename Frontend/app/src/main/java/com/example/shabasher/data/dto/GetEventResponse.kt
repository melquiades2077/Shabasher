package com.example.shabasher.data.dto

import kotlinx.serialization.Serializable

// Получение события по ID
@Serializable
data class GetEventResponse(
    val id: String,
    val title: String? = null,
    val description: String? = null,
    val address: String? = null,
    val dateTime: String? = null,
    val creatorId: String? = null,
    val createdAt: String? = null,
    val participants: List<EventParticipantDto> = emptyList()
)