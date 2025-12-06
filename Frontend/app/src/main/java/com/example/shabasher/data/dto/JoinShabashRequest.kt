package com.example.shabasher.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class JoinShabashRequest(
    val shabashId: String
)