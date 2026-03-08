package com.example.shabasher.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shabasher.Model.Donation
import com.example.shabasher.data.network.DonationsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DonationViewModel : ViewModel() {
    private val repository = DonationsRepository()
    private val _uiState = MutableStateFlow(DonationState())
    val uiState: StateFlow<DonationState> = _uiState

    // В будущем — вызов репозитория или API
    fun onPaymentConfirmed() {
        // TODO: отправить запрос на сервер
    }

    fun copyPaymentDetails(): String {
        return uiState.value.donation!!.paymentDetails
    }

    fun loadDonationById(donationId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(donation = repository.getDonationById(donationId)) }
        }
    }
}

data class DonationState(
    val donation: Donation? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)