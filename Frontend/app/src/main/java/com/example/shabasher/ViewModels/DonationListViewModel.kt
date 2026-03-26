package com.example.shabasher.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.shabasher.Model.Donation
import com.example.shabasher.data.dto.Fundraise
import com.example.shabasher.data.network.DonationsRepository
import com.example.shabasher.data.network.FundraisesRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DonationListState(
    val donations: List<Fundraise> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

// ================= EVENTS =================

sealed class DonationListEvent {
    data class ShowSnackbar(val message: String) : DonationListEvent()
}

// ================= VIEWMODEL =================

class DonationListViewModel(
    private val repository: FundraisesRepository,
    val eventId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(DonationListState(isLoading = true))
    val uiState: StateFlow<DonationListState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<DonationListEvent>()
    val events = _events.asSharedFlow()

    init {
        loadDonations()
    }

    fun loadDonations() {
        viewModelScope.launch {

            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = repository.getAllFundraises(eventId)

            val data = result.getOrNull()
            val error = result.exceptionOrNull()

            if (data != null) {
                _uiState.update {
                    it.copy(
                        donations = data,
                        isLoading = false
                    )
                }
            } else {
                val message = getErrorMessage(error)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = message
                    )
                }

                _events.emit(DonationListEvent.ShowSnackbar(message))
            }
        }
    }

    // 🔥 нормальная обработка ошибок (такая же как у тебя в другом VM)
    private fun getErrorMessage(e: Throwable?): String {
        return when (e) {
            is SecurityException -> "❌ Нет доступа"
            is NoSuchElementException -> "❌ Событие не найдено"
            is IllegalArgumentException -> "❌ ${e.message}"
            is IllegalStateException -> "⚠️ ${e.message}"
            else -> "⚠️ ${e?.message ?: "Ошибка"}"
        }
    }
}

class DonationListViewModelFactory(
    private val repository: FundraisesRepository,
    private val eventId: String
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DonationListViewModel::class.java)) {
            return DonationListViewModel(repository, eventId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}