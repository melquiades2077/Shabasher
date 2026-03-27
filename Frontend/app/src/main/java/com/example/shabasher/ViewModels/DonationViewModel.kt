package com.example.shabasher.ViewModels


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shabasher.data.network.FundraisesRepository
import com.example.shabasher.data.dto.Fundraise
import com.example.shabasher.data.dto.FundraiseParticipant
import com.example.shabasher.data.dto.FundraiseParticipantStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal

// ═══════════════════════════════════════════════════════
// UI State
// ═══════════════════════════════════════════════════════
sealed interface DonationUiState {
    object Loading : DonationUiState
    data class Success(val donation: Fundraise) : DonationUiState
    data class Error(val message: String, val retry: () -> Unit = {}) : DonationUiState
}

// ═══════════════════════════════════════════════════════
// Action State (для тостов/снэков)
// ═══════════════════════════════════════════════════════
data class DonationActionState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: String? = null
)

// ═══════════════════════════════════════════════════════
// ViewModel
// ═══════════════════════════════════════════════════════
class DonationViewModel(
    private val repository: FundraisesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DonationUiState>(DonationUiState.Loading)
    val uiState: StateFlow<DonationUiState> = _uiState.asStateFlow()

    private val _actionState = MutableStateFlow(DonationActionState())
    val actionState: StateFlow<DonationActionState> = _actionState.asStateFlow()

    private var currentFundraiseId: String? = null

    // 🔹 Загрузка сбора по ID
    fun loadDonationById(fundraiseId: String) {
        currentFundraiseId = fundraiseId
        viewModelScope.launch {
            _uiState.value = DonationUiState.Loading
            repository.getFundraiseDetails(fundraiseId)
                .onSuccess { fundraise ->
                    _uiState.value = DonationUiState.Success(fundraise)
                }
                .onFailure { error ->
                    Log.e(TAG, "Failed to load fundraise", error)
                    _uiState.value = DonationUiState.Error(
                        message = error.message ?: "Не удалось загрузить сбор",
                        retry = { loadDonationById(fundraiseId) }
                    )
                }
        }
    }

    // 🔹 Участник отмечает оплату
    fun markPaid(onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        val fundraiseId = currentFundraiseId ?: return
        viewModelScope.launch {
            _actionState.update { it.copy(isLoading = true, error = null, success = null) }
            repository.markPaid(fundraiseId)
                .onSuccess {
                    _actionState.update { it.copy(isLoading = false, success = "Оплата отмечена!") }
                    loadDonationById(fundraiseId) // обновляем данные
                    onSuccess()
                }
                .onFailure { error ->
                    Log.e(TAG, "Failed to mark paid", error)
                    val message = when (error) {
                        is IllegalStateException -> "Вы уже отметили оплату"
                        is SecurityException -> "Нет доступа"
                        is NoSuchElementException -> "Сбор не найден"
                        else -> error.message ?: "Ошибка при отметке оплаты"
                    }
                    _actionState.update { it.copy(isLoading = false, error = message) }
                    onError(message)
                }
        }
    }

    // 🔹 Админ подтверждает оплату участника
    fun confirmPayment(
        participantUserId: String,
        amount: BigDecimal?,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val fundraiseId = currentFundraiseId ?: return
        viewModelScope.launch {
            _actionState.update { it.copy(isLoading = true, error = null, success = null) }
            repository.confirmPayment(fundraiseId, participantUserId, amount)
                .onSuccess {
                    _actionState.update { it.copy(isLoading = false, success = "Оплата подтверждена!") }
                    loadDonationById(fundraiseId)
                    onSuccess()
                }
                .onFailure { error ->
                    Log.e(TAG, "Failed to confirm payment", error)
                    val message = when (error) {
                        is SecurityException -> "Только администраторы могут подтверждать"
                        is NoSuchElementException -> "Участник или сбор не найден"
                        is IllegalArgumentException -> error.message ?: "Неверные данные"
                        else -> error.message ?: "Ошибка при подтверждении"
                    }
                    _actionState.update { it.copy(isLoading = false, error = message) }
                    onError(message)
                }
        }
    }

    // 🔹 Админ отменяет подтверждение оплаты
    fun revertPayment(
        participantUserId: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val fundraiseId = currentFundraiseId ?: return
        viewModelScope.launch {
            _actionState.update { it.copy(isLoading = true, error = null, success = null) }
            repository.revertPayment(fundraiseId, participantUserId)
                .onSuccess {
                    _actionState.update { it.copy(isLoading = false, success = "Подтверждение отменено") }
                    loadDonationById(fundraiseId)
                    onSuccess()
                }
                .onFailure { error ->
                    Log.e(TAG, "Failed to revert payment", error)
                    val message = when (error) {
                        is SecurityException -> "Только администраторы могут отменять"
                        is NoSuchElementException -> "Участник или сбор не найден"
                        else -> error.message ?: "Ошибка при отмене"
                    }
                    _actionState.update { it.copy(isLoading = false, error = message) }
                    onError(message)
                }
        }
    }

    // 🔹 Админ закрывает сбор
    fun closeFundraise(
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val fundraiseId = currentFundraiseId ?: return
        viewModelScope.launch {
            _actionState.update { it.copy(isLoading = true, error = null, success = null) }
            repository.closeFundraise(fundraiseId)
                .onSuccess {
                    _actionState.update { it.copy(isLoading = false, success = "Сбор закрыт") }
                    loadDonationById(fundraiseId)
                    onSuccess()
                }
                .onFailure { error ->
                    Log.e(TAG, "Failed to close fundraise", error)
                    val message = when (error) {
                        is SecurityException -> "Только администраторы могут закрывать"
                        is NoSuchElementException -> "Сбор не найден"
                        else -> error.message ?: "Ошибка при закрытии"
                    }
                    _actionState.update { it.copy(isLoading = false, error = message) }
                    onError(message)
                }
        }
    }

    // 🔹 Очистка состояния действий (после показа тоста)
    fun clearActionState() {
        _actionState.update { it.copy(error = null, success = null) }
    }

    // 🔹 Принудительное обновление
    fun refresh() {
        currentFundraiseId?.let { loadDonationById(it) }
    }

    // 🔹 Получение текущего userId (для проверок в UI)
    fun getCurrentUserId(): String? = repository.getCurrentUserId()

    override fun onCleared() {
        super.onCleared()
        repository.close()
    }

    companion object {
        private const val TAG = "DonationViewModel"
    }
}