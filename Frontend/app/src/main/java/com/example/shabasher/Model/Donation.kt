package com.example.shabasher.Model

data class Donation(
    val title: String,
    val description: String,
    val collectedAmount: Int,
    val targetAmount: Int,
    val paidParticipants: Int,
    val totalParticipants: Int,
    val paymentDetails: String,
    val organizerName: String
)
