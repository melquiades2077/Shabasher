package com.example.shabasher.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.shabasher.data.dto.Fundraise
import com.example.shabasher.data.network.FundraisesRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal

// ================= STATE =================

sealed class FundraiseUiState {
    object Loading : FundraiseUiState()

    data class Success(
        val fundraises: List<Fundraise> = emptyList(),
        val selectedFundraise: Fundraise? = null,
        val isLoadingDetails: Boolean = false
    ) : FundraiseUiState()

    data class Error(
        val message: String,
        val retry: () -> Unit
    ) : FundraiseUiState()
}

// ================= EVENTS =================

sealed class FundraiseEvent {
    data class ShowSnackbar(
        val message: String,
        val actionLabel: String? = null,
        val onAction: (() -> Unit)? = null
    ) : FundraiseEvent()

    data class NavigateToDetails(val id: String) : FundraiseEvent()
}

// ================= ACTIONS =================

sealed class FundraiseAction {
    object Load : FundraiseAction()
    data class LoadDetails(val id: String) : FundraiseAction()
    data class Create(
        val title: String,
        val description: String?,
        val target: BigDecimal?,
        val phone: String,
        val recipient: String
    ) : FundraiseAction()

    data class Close(val id: String) : FundraiseAction()
    data class MarkPaid(val id: String) : FundraiseAction()
    data class Confirm(val fundraiseId: String, val userId: String, val amount: BigDecimal?) : FundraiseAction()
    data class Revert(val fundraiseId: String, val userId: String) : FundraiseAction()
}

// ================= VIEWMODEL =================

class FundraisesViewModel(
    private val repository: FundraisesRepository,
    private val shabashId: String
) : ViewModel() {

    private companion object {
        private const val TAG = "FundraisesVM"
    }

    // STATE
    private val _uiState = MutableStateFlow<FundraiseUiState>(FundraiseUiState.Loading)
    val uiState = _uiState.asStateFlow()

    // EVENTS (🔥 правильно)
    private val _events = MutableSharedFlow<FundraiseEvent>()
    val events = _events.asSharedFlow()

    val currentUserId: String? get() = repository.getCurrentUserId()

    init {
        loadFundraises()
    }

    // ================= ACTION HANDLER =================

    fun onAction(action: FundraiseAction) {
        when (action) {
            is FundraiseAction.Load -> loadFundraises()
            is FundraiseAction.LoadDetails -> loadDetails(action.id)
            is FundraiseAction.Create -> createFundraise(action)
            is FundraiseAction.Close -> closeFundraise(action.id)
            is FundraiseAction.MarkPaid -> markPaid(action.id)
            is FundraiseAction.Confirm -> confirmPayment(action)
            is FundraiseAction.Revert -> revertPayment(action)
        }
    }

    // ================= LOAD LIST =================

    private fun loadFundraises() {
        viewModelScope.launch {

            _uiState.update {
                if (it is FundraiseUiState.Success) it else FundraiseUiState.Loading
            }

            repository.getAllFundraises(shabashId).handle(
                onSuccess = { data ->
                    _uiState.update {
                        FundraiseUiState.Success(
                            fundraises = data,
                            selectedFundraise = (it as? FundraiseUiState.Success)?.selectedFundraise
                        )
                    }
                },
                onError = { error ->
                    val msg = getErrorMessage(error)
                    _uiState.value = FundraiseUiState.Error(msg) { loadFundraises() }
                }
            )
        }
    }

    // ================= LOAD DETAILS =================

    private fun loadDetails(id: String) {
        viewModelScope.launch {

            _uiState.update {
                (it as? FundraiseUiState.Success)?.copy(isLoadingDetails = true) ?: it
            }

            repository.getFundraiseDetails(id).handle(
                onSuccess = { data ->
                    _uiState.update {
                        (it as? FundraiseUiState.Success)?.copy(
                            selectedFundraise = data,
                            isLoadingDetails = false
                        ) ?: FundraiseUiState.Success(selectedFundraise = data)
                    }
                },
                onError = {
                    sendEvent(FundraiseEvent.ShowSnackbar(getErrorMessage(it)))
                }
            )
        }
    }

    // ================= CREATE =================

    private fun createFundraise(action: FundraiseAction.Create) {
        viewModelScope.launch {

            repository.createFundraise(
                shabashId = shabashId,
                title = action.title,
                description = action.description,
                targetAmount = action.target,
                paymentPhone = action.phone,
                paymentRecipient = action.recipient
            ).handle(
                onSuccess = { data ->
                    sendEvent(
                        FundraiseEvent.ShowSnackbar(
                            "✅ Сбор создан",
                            "Открыть"
                        ) {
                            viewModelScope.launch {
                                _events.emit(FundraiseEvent.NavigateToDetails(data.id))
                            }
                        }
                    )
                    loadFundraises()
                },
                onError = {
                    sendEvent(FundraiseEvent.ShowSnackbar(getErrorMessage(it)))
                }
            )
        }
    }

    // ================= CLOSE =================

    private fun closeFundraise(id: String) {
        viewModelScope.launch {
            repository.closeFundraise(id).handle(
                onSuccess = {
                    sendEvent(FundraiseEvent.ShowSnackbar("🔒 Сбор закрыт"))
                    loadDetails(id)
                    loadFundraises()
                },
                onError = {
                    sendEvent(FundraiseEvent.ShowSnackbar(getErrorMessage(it)))
                }
            )
        }
    }

    // ================= MARK PAID =================

    private fun markPaid(id: String) {
        viewModelScope.launch {
            repository.markPaid(id).handle(
                onSuccess = {
                    sendEvent(FundraiseEvent.ShowSnackbar("✅ Отмечено, ждите подтверждения"))
                    loadDetails(id)
                    loadFundraises()
                },
                onError = {
                    sendEvent(FundraiseEvent.ShowSnackbar(getErrorMessage(it)))
                }
            )
        }
    }

    // ================= CONFIRM =================

    private fun confirmPayment(action: FundraiseAction.Confirm) {
        viewModelScope.launch {
            repository.confirmPayment(
                action.fundraiseId,
                action.userId,
                action.amount
            ).handle(
                onSuccess = {
                    sendEvent(FundraiseEvent.ShowSnackbar("✅ Подтверждено"))
                    loadDetails(action.fundraiseId)
                },
                onError = {
                    sendEvent(FundraiseEvent.ShowSnackbar(getErrorMessage(it)))
                }
            )
        }
    }

    // ================= REVERT =================

    private fun revertPayment(action: FundraiseAction.Revert) {
        viewModelScope.launch {
            repository.revertPayment(
                action.fundraiseId,
                action.userId
            ).handle(
                onSuccess = {
                    sendEvent(FundraiseEvent.ShowSnackbar("🔄 Отменено"))
                    loadDetails(action.fundraiseId)
                },
                onError = {
                    sendEvent(FundraiseEvent.ShowSnackbar(getErrorMessage(it)))
                }
            )
        }
    }

    // ================= HELPERS =================

    private suspend fun sendEvent(event: FundraiseEvent) {
        _events.emit(event)
    }

    private inline fun <T> Result<T>.handle(
        onSuccess: (T) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val data = getOrNull()
        val error = exceptionOrNull()

        if (data != null) onSuccess(data)
        else onError(error ?: Exception("Unknown error"))
    }

    private fun getErrorMessage(e: Throwable): String {
        return when (e) {
            is SecurityException -> "❌ Нет прав"
            is NoSuchElementException -> "❌ Не найдено"
            is IllegalArgumentException -> "❌ ${e.message}"
            is IllegalStateException -> "⚠️ ${e.message}"
            else -> "⚠️ ${e.message ?: "Ошибка"}"
        }
    }
}


class FundraisesViewModelFactory(
    private val repository: FundraisesRepository,
    private val shabashId: String
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FundraisesViewModel::class.java)) {
            return FundraisesViewModel(repository, shabashId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}