package com.example.shabasher.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shabasher.Model.Donation
import com.example.shabasher.Model.DonationPaymentStatus
import com.example.shabasher.Model.DonationStatus
import com.example.shabasher.data.network.DonationsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DonationListViewModel() : ViewModel() {
    private val repository = DonationsRepository()
    private val _uiState = MutableStateFlow(DonationListState())
    val uiState: StateFlow<DonationListState> = _uiState

    init {
        fetchDonations()
    }

    fun fetchDonations() {
        viewModelScope.launch {
            _uiState.update { it.copy(donations = repository.getDonations()) }
        }
    }
}

data class DonationListState(
    val donations: List<Donation> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)