package com.example.shabasher.data.dto

import kotlinx.serialization.Serializable

// Получение события по ID
@Serializable
data class GetEventResponse(
    val id: String,
    val name: String? = null,
    val description: String? = null,
    val address: String? = null,
    val startDate: String? = null,
    val startTime: String? = null,
    val dateTime: String? = null,
    val creatorId: String? = null,
    val createdAt: String? = null,
    val participants: List<EventParticipantDto> = emptyList(),
    val currentUserRole: String? = null,
    val currentUserParticipationStatus: String? = null
)