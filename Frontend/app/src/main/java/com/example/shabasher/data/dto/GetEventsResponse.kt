package com.example.shabasher.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class GetEventsResponse(
    val events: List<EventShortDto>
)