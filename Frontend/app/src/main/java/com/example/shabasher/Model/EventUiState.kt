package com.example.shabasher.Model



data class EventData(
    val id: String,
    val title: String,
    val description: String,
    val date: String,
    val place: String,
    val time: String,
    val participants: List<Participant>,
    val userStatus: ParticipationStatus = ParticipationStatus.INVITED
)
