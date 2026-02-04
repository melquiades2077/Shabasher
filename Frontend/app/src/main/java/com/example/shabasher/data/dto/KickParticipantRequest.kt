package com.example.shabasher.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class KickParticipantRequest(
    val shabashId: String,
    val userId: String,
    val adminId: String // ID того, кто выгоняет
)