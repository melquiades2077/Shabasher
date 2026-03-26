package com.example.shabasher.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shabasher.data.network.FundraisesRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal

class CreateFundraiseViewModel(
    private val repository: FundraisesRepository,
    private val shabashId: String
) : ViewModel() {

    private val _state = MutableStateFlow<CreateFundraiseState>(CreateFundraiseState.Idle)
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<FundraiseEvent>()
    val events = _events.asSharedFlow()

    fun create(
        title: String,
        description: String?,
        target: BigDecimal?,
        phone: String,
        recipient: String
    ) {
        // 👇 Валидация на уровне ViewModel
        when {
            title.isBlank() -> {
                viewModelScope.launch {
                    _events.emit(FundraiseEvent.ShowSnackbar("❌ Введите название сбора"))
                }
                return
            }
            phone.isBlank() -> {
                viewModelScope.launch {
                    _events.emit(FundraiseEvent.ShowSnackbar("❌ Введите телефон для оплаты"))
                }
                return
            }
            recipient.isBlank() -> {
                viewModelScope.launch {
                    _events.emit(FundraiseEvent.ShowSnackbar("❌ Введите получателя"))
                }
                return
            }
        }

        viewModelScope.launch {
            _state.value = CreateFundraiseState.Loading

            repository.createFundraise(
                shabashId = shabashId,
                title = title.trim(),
                description = description?.trim(),
                targetAmount = target,
                paymentPhone = phone.trim(),
                paymentRecipient = recipient.trim()
            )
                .onSuccess { result ->
                    _state.value = CreateFundraiseState.Idle
                    _events.emit(FundraiseEvent.NavigateToDetails(result.id))
                }
                .onFailure { error ->
                    _state.value = CreateFundraiseState.Error(error.message ?: "Ошибка")
                    _events.emit(FundraiseEvent.ShowSnackbar(error.message ?: "Ошибка"))
                }
        }
    }
}

sealed class CreateFundraiseState {
    object Idle : CreateFundraiseState()
    object Loading : CreateFundraiseState()
    data class Error(val message: String) : CreateFundraiseState()
}