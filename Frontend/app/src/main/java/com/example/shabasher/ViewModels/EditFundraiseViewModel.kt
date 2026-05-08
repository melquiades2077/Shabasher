package com.example.shabasher.ViewModels

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.shabasher.data.network.FundraisesRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal

class EditFundraiseViewModel(
    private val repository: FundraisesRepository
) : ViewModel() {

    private val _uiState = mutableStateOf(EditFundraiseUiState())
    val uiState: State<EditFundraiseUiState> = _uiState

    /** Эмитится при успешном сохранении — экран сам popBackStack(). */
    private val _saved = MutableSharedFlow<Unit>()
    val saved = _saved.asSharedFlow()

    fun loadFundraise(fundraiseId: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null, fundraiseId = fundraiseId)
        viewModelScope.launch {
            val result = repository.getFundraiseDetails(fundraiseId)
            if (result.isSuccess) {
                val f = result.getOrNull()!!
                val target = f.targetAmount?.stripTrailingZeros()?.toPlainString() ?: ""
                _uiState.value = EditFundraiseUiState(
                    fundraiseId = f.id,
                    title = f.title,
                    description = f.description.orEmpty(),
                    targetAmount = target,
                    paymentPhone = f.paymentPhone,
                    paymentRecipient = f.paymentRecipient,
                    originalTitle = f.title,
                    originalDescription = f.description.orEmpty(),
                    originalTargetAmount = target,
                    originalPaymentPhone = f.paymentPhone,
                    originalPaymentRecipient = f.paymentRecipient,
                    isLoading = false
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Не удалось загрузить сбор"
                )
            }
        }
    }

    fun updateTitle(v: String) { _uiState.value = _uiState.value.copy(title = v) }
    fun updateDescription(v: String) { _uiState.value = _uiState.value.copy(description = v) }
    fun updateTargetAmount(v: String) {
        _uiState.value = _uiState.value.copy(targetAmount = v.filter { it.isDigit() || it == '.' || it == ',' })
    }
    fun updatePaymentPhone(v: String) { _uiState.value = _uiState.value.copy(paymentPhone = v) }
    fun updatePaymentRecipient(v: String) { _uiState.value = _uiState.value.copy(paymentRecipient = v) }

    fun save() {
        val s = _uiState.value
        val id = s.fundraiseId ?: return
        if (s.title.isBlank()) {
            _uiState.value = s.copy(error = "Название не может быть пустым")
            return
        }
        if (s.paymentPhone.isBlank()) {
            _uiState.value = s.copy(error = "Телефон для оплаты не может быть пустым")
            return
        }
        val parsedTarget = if (s.targetAmount.isBlank()) null
        else s.targetAmount.replace(',', '.').toBigDecimalOrNull()
        if (s.targetAmount.isNotBlank() && parsedTarget == null) {
            _uiState.value = s.copy(error = "Целевая сумма должна быть числом")
            return
        }

        _uiState.value = s.copy(isLoading = true, error = null)

        viewModelScope.launch {
            val result = repository.updateFundraise(
                fundraiseId = id,
                title = s.title.trim(),
                description = s.description.trim().ifBlank { null },
                targetAmount = parsedTarget,
                paymentPhone = s.paymentPhone.trim(),
                paymentRecipient = s.paymentRecipient.trim()
            )
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    originalTitle = s.title,
                    originalDescription = s.description,
                    originalTargetAmount = s.targetAmount,
                    originalPaymentPhone = s.paymentPhone,
                    originalPaymentRecipient = s.paymentRecipient
                )
                _saved.emit(Unit)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Не удалось сохранить"
                )
                Log.e(TAG, "save failed", result.exceptionOrNull())
            }
        }
    }

    companion object { private const val TAG = "EditFundraiseVM" }
}

class EditFundraiseViewModelFactory(
    private val repository: FundraisesRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditFundraiseViewModel::class.java)) {
            return EditFundraiseViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

data class EditFundraiseUiState(
    val fundraiseId: String? = null,
    val title: String = "",
    val description: String = "",
    val targetAmount: String = "",
    val paymentPhone: String = "",
    val paymentRecipient: String = "",

    val originalTitle: String = "",
    val originalDescription: String = "",
    val originalTargetAmount: String = "",
    val originalPaymentPhone: String = "",
    val originalPaymentRecipient: String = "",

    val isLoading: Boolean = false,
    val error: String? = null
) {
    val isDirty: Boolean
        get() = title != originalTitle ||
                description != originalDescription ||
                targetAmount != originalTargetAmount ||
                paymentPhone != originalPaymentPhone ||
                paymentRecipient != originalPaymentRecipient
}
