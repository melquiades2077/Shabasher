package com.example.shabasher.Model

data class EventFull(
    val id: String,
    val title: String,
    val description: String,
    val address: String,
    val date: String,
    val time: String,
    val participants: List<Participant>
)
