package com.example.shabasher.ViewModels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DonationViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DonationState())
    val uiState: StateFlow<DonationState> = _uiState

    // В будущем — вызов репозитория или API
    fun onPaymentConfirmed() {
        // TODO: отправить запрос на сервер
    }

    fun copyPaymentDetails(): String {
        return uiState.value.paymentDetails
    }
}

data class DonationState(
    val title: String = "Поездка в горы на выходные",
    val description: String = "Сбор на транспорт и проживание для группы из 12 человек",
    val collectedAmount: Int = 13_000,
    val targetAmount: Int = 20_000,
    val paidParticipants: Int = 5,
    val totalParticipants: Int = 12,
    val paymentDetails: String = "Tinkoff •••• 1234\nАлексей Петров",
    val organizerName: String = "Алексей Петров"
)