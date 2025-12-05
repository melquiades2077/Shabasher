package com.example.shabasher.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class JoinEventRequest(
    val shabashId: String
)