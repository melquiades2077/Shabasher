package com.example.shabasher.ViewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.shabasher.Model.UserRole
import com.example.shabasher.data.dto.Fundraise
import com.example.shabasher.data.network.EventsRepository
import com.example.shabasher.data.network.FundraisesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import java.math.BigDecimal

// ═══════════════════════════════════════════════════════
// UI State
// ═══════════════════════════════════════════════════════
sealed interface DonationUiState {
    data object Loading : DonationUiState
    data class Success(
        val donation: Fundraise,
        val currentUserRole: UserRole = UserRole.MEMBER
    ) : DonationUiState

    data class Error(val message: String, val retry: () -> Unit = {}) : DonationUiState
}

/** Можно ли пользователю с такой ролью подтверждать оплаты / закрывать сбор. */
fun UserRole.canManageFundraise(): Boolean =
    this == UserRole.ADMIN || this == UserRole.MODERATOR

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
    private val repository: FundraisesRepository,
    private val eventsRepository: EventsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DonationUiState>(DonationUiState.Loading)
    val uiState: StateFlow<DonationUiState> = _uiState.asStateFlow()

    private val _actionState = MutableStateFlow(DonationActionState())
    val actionState: StateFlow<DonationActionState> = _actionState.asStateFlow()

    private var currentFundraiseId: String? = null

    /** Загрузка сбора + роль пользователя в событии. */
    fun loadDonationById(fundraiseId: String) {
        currentFundraiseId = fundraiseId
        viewModelScope.launch {
            _uiState.value = DonationUiState.Loading
            repository.getFundraiseDetails(fundraiseId)
                .onSuccess { fundraise ->
                    val role = resolveCurrentUserRole(fundraise.shabashId, fundraise.isCreator)
                    _uiState.value = DonationUiState.Success(fundraise, role)
                }
                .onFailure { error ->
                    Log.e(TAG, "Failed to load fundraise", error)
                    _uiState.value = DonationUiState.Error(
                        message = mapLoadError(error),
                        retry = { loadDonationById(fundraiseId) }
                    )
                }
        }
    }

    /** Определить роль текущего пользователя в событии. */
    private suspend fun resolveCurrentUserRole(shabashId: String, isCreator: Boolean): UserRole {
        // Тихий fallback: если событие не достанем — считаем создателя сбора админом, остальных Member.
        return try {
            val response = eventsRepository.getEventById(shabashId).getOrNull()
                ?: return if (isCreator) UserRole.ADMIN else UserRole.MEMBER

            val currentUserId = eventsRepository.getCurrentUserId()
                ?: return if (isCreator) UserRole.ADMIN else UserRole.MEMBER

            val raw = response.participants
                .firstOrNull { it.user.id == currentUserId }
                ?.role

            when (raw) {
                "Admin" -> UserRole.ADMIN
                "CoAdmin", "Moderator" -> UserRole.MODERATOR
                else -> if (isCreator) UserRole.ADMIN else UserRole.MEMBER
            }
        } catch (e: Exception) {
            Log.w(TAG, "Не удалось определить роль, fallback to creator-check", e)
            if (isCreator) UserRole.ADMIN else UserRole.MEMBER
        }
    }

    fun markPaid() {
        val fundraiseId = currentFundraiseId ?: return
        runAction("Готово, ждите подтверждения") {
            repository.markPaid(fundraiseId)
        }
    }

    fun confirmPayment(participantUserId: String, amount: BigDecimal?) {
        val fundraiseId = currentFundraiseId ?: return
        runAction("Оплата подтверждена") {
            repository.confirmPayment(fundraiseId, participantUserId, amount)
        }
    }

    fun revertPayment(participantUserId: String) {
        val fundraiseId = currentFundraiseId ?: return
        runAction("Подтверждение отменено") {
            repository.revertPayment(fundraiseId, participantUserId)
        }
    }

    fun closeFundraise() {
        val fundraiseId = currentFundraiseId ?: return
        runAction("Сбор закрыт") {
            repository.closeFundraise(fundraiseId)
        }
    }

    /** Универсальный обработчик действий с автоматическим refresh. */
    private inline fun runAction(
        successMessage: String,
        crossinline block: suspend () -> Result<*>
    ) {
        viewModelScope.launch {
            _actionState.update { it.copy(isLoading = true, error = null, success = null) }
            block()
                .onSuccess {
                    _actionState.update { it.copy(isLoading = false, success = successMessage) }
                    currentFundraiseId?.let { reloadAfterAction(it) }
                }
                .onFailure { error ->
                    Log.e(TAG, "Action failed", error)
                    _actionState.update { it.copy(isLoading = false, error = mapActionError(error)) }
                }
        }
    }

    /** Перезагружает только данные сбора, не сбрасывая UI в Loading. */
    private fun reloadAfterAction(fundraiseId: String) {
        viewModelScope.launch {
            repository.getFundraiseDetails(fundraiseId)
                .onSuccess { fundraise ->
                    val current = _uiState.value
                    val role = (current as? DonationUiState.Success)?.currentUserRole
                        ?: resolveCurrentUserRole(fundraise.shabashId, fundraise.isCreator)
                    _uiState.value = DonationUiState.Success(fundraise, role)
                }
                .onFailure { error ->
                    Log.w(TAG, "Refresh failed after action", error)
                }
        }
    }

    private fun mapLoadError(error: Throwable): String = when (error) {
        is SecurityException -> "Нет доступа к сбору"
        is NoSuchElementException -> "Сбор не найден"
        is IOException -> "Нет связи с сервером. Проверьте интернет"
        else -> when {
            error.message?.contains("connection", ignoreCase = true) == true -> "Нет связи с сервером"
            error.message?.contains("timeout", ignoreCase = true) == true -> "Сервер не отвечает"
            else -> error.message ?: "Не удалось загрузить сбор"
        }
    }

    private fun mapActionError(error: Throwable): String = when (error) {
        is SecurityException -> error.message ?: "Нет прав"
        is NoSuchElementException -> error.message ?: "Не найдено"
        is IllegalStateException -> error.message ?: "Действие уже выполнено"
        is IllegalArgumentException -> error.message ?: "Неверные данные"
        is IOException -> "Нет связи с сервером"
        else -> error.message ?: "Ошибка"
    }

    fun clearActionState() {
        _actionState.update { it.copy(error = null, success = null) }
    }

    fun refresh() {
        currentFundraiseId?.let { loadDonationById(it) }
    }

    fun getCurrentUserId(): String? = repository.getCurrentUserId()

    override fun onCleared() {
        super.onCleared()
        repository.close()
    }

    companion object {
        private const val TAG = "DonationViewModel"
    }
}

class DonationViewModelFactory(
    private val repository: FundraisesRepository,
    private val eventsRepository: EventsRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DonationViewModel::class.java)) {
            return DonationViewModel(repository, eventsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
