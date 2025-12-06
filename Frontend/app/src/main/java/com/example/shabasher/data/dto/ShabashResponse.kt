package com.example.shabasher.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ShabashResponse(
    val id: String,
    val name: String,
    val description: String,
    val address: String,
    val startDate: String,
    val startTime: String,
    val createdAt: String,
    val status: String,
    val participants: List<ShabashParticipantDto> = emptyList()
)