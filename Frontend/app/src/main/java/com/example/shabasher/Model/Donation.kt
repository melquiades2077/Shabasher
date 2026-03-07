package com.example.shabasher.Model

enum class DonationStatus { ACTIVE, COMPLETED, CLOSED }
enum class DonationPaymentStatus { PAID, NOT_PAID }
data class Donation(
    val id: String,
    val title: String,
    val description: String,
    val status: DonationStatus,
    val paymentStatus: DonationPaymentStatus,
    val collectedAmount: Int,
    val targetAmount: Int,
    val paidParticipants: Int,
    val totalParticipants: Int,
    val paymentDetails: String,
    val organizerName: String
)
