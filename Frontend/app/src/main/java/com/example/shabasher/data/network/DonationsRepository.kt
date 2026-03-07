package com.example.shabasher.data.network

import com.example.shabasher.Model.Donation
import com.example.shabasher.Model.DonationPaymentStatus
import com.example.shabasher.Model.DonationStatus

class DonationsRepository {
    // val mockDonations = emptyList<Donation>()
    val mockDonations =  listOf(
        Donation(
            id = "1",
            title = "Поездка в горы на выходные",
            description = "Сбор на транспорт и проживание для группы из 12 человек",
            status = DonationStatus.ACTIVE,
            paymentStatus = DonationPaymentStatus.PAID,
            collectedAmount = 13_000,
            targetAmount = 20_000,
            paidParticipants = 5,
            totalParticipants = 12,
            paymentDetails = "Tinkoff •••• 1234\nАлексей Петров",
            organizerName = "Алексей Петров"
        ),
        Donation(
            id = "2",
            title = "Сорваться к морю",
            description = "Сбор на транспорт и проживание для группы из 12 человек",
            status = DonationStatus.CLOSED,
            paymentStatus = DonationPaymentStatus.NOT_PAID,
            collectedAmount = 5_000,
            targetAmount = 30_000,
            paidParticipants = 10,
            totalParticipants = 15,
            paymentDetails = "Tinkoff •••• 1234\nАлексей Петров",
            organizerName = "Алексей Петров"
        )
    )

    suspend fun getDonations(): List<Donation> {
        return mockDonations
    }

    suspend fun getDonationById(id: String): Donation? {
        return mockDonations.find { it.id == id }
    }
}