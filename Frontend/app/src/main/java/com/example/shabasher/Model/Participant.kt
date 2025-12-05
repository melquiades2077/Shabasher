package com.example.shabasher.Model


enum class ParticipationStatus { GOING, NOT_GOING, INVITED }

data class Participant(
    val id: String,
    val name: String,
    val status: ParticipationStatus
)