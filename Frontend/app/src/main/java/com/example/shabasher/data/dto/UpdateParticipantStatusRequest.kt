package com.example.shabasher.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class StatusUpdateRequest(
    val shabashId: String,
    val status: Int
)
