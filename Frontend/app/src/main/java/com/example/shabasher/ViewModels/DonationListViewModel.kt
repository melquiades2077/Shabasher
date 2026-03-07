package com.example.shabasher.ViewModels

import androidx.lifecycle.ViewModel
import com.example.shabasher.Model.Donation
import com.example.shabasher.Model.DonationPaymentStatus
import com.example.shabasher.Model.DonationStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DonationListViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DonationListState())
    val uiState: StateFlow<DonationListState> = _uiState

    fun fetchDonations() {
        // val mockDonations = emptyList<Donation>()
        val mockDonations =  listOf(
            Donation(
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
        _uiState.value = _uiState.value.copy(donations = mockDonations)
    }

    init {
        fetchDonations()
    }
}

data class DonationListState(
    val donations: List<Donation> = emptyList()
)