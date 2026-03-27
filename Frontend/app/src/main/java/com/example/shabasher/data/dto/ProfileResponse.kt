package com.example.shabasher.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProfileResponse(
    @SerialName("id")
    val id: String,

    @SerialName("name")
    val name: String,

    @SerialName("email")
    val email: String,

    @SerialName("createdAt")
    val createdAt: String? = null,

    @SerialName("aboutMe")
    val aboutMe: String? = null,

    @SerialName("telegram")
    val telegram: String? = null,

    @SerialName("participations")
    val participations: List<ParticipationDto> = emptyList()
)

@Serializable
data class ParticipationDto(
    val shabashId: String,
    val shabashName: String?,
    val role: String? // ← КЛЮЧЕВОЕ ПОЛЕ!
)