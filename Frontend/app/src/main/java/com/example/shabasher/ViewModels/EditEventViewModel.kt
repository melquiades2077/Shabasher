package com.example.shabasher.ViewModels

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shabasher.Model.EventData
import com.example.shabasher.data.network.EventsRepository
import kotlinx.coroutines.launch
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModelProvider

class EditEventViewModel(
    context: Context
) : ViewModel() {

    private val repository = EventsRepository(context)
    private val _uiState = mutableStateOf(EditEventUiState())
    val uiState: State<EditEventUiState> = _uiState

    fun loadEventForEdit(event: EventData) {
        _uiState.value = _uiState.value.copy(
            eventId = event.id,
            title = event.title,
            description = event.description,
            address = event.place,
            date = event.date,
            time = event.time,
            isLoading = false
        )
    }

    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
    }

    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    fun updateAddress(address: String) {
        _uiState.value = _uiState.value.copy(address = address)
    }

    fun setDate(date: String) {
        _uiState.value = _uiState.value.copy(date = date)
    }

    fun setTime(hour: Int, minute: Int) {
        val formattedTime = String.format("%02d:%02d", hour, minute)
        _uiState.value = _uiState.value.copy(time = formattedTime)
    }

    fun saveEvent() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        val request = EventsRepository.UpdateEventRequest(
            id = _uiState.value.eventId ?: return,
            name = _uiState.value.title,
            description = _uiState.value.description,
            address = _uiState.value.address,
            startDate = _uiState.value.date,
            startTime = "${_uiState.value.time}:00"
        )

        viewModelScope.launch {
            val result = repository.updateEvent(request)
            if (result.isSuccess) {
                // ✅ ОБНОВЛЯЕМ ОРИГИНАЛЬНЫЕ ЗНАЧЕНИЯ ПОСЛЕ УСПЕШНОГО СОХРАНЕНИЯ
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    originalTitle = _uiState.value.title,
                    originalDescription = _uiState.value.description,
                    originalAddress = _uiState.value.address,
                    originalDate = _uiState.value.date,
                    originalTime = _uiState.value.time
                )
                // Опционально: можно вернуться назад
                // onEventSaved?.invoke()
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Не удалось сохранить изменения: ${result.exceptionOrNull()?.message}"
                )
            }
        }
    }

    fun loadEventById(eventId: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val result = repository.getEventById(eventId)
            if (result.isSuccess && result.getOrNull() != null) {
                val dto = result.getOrNull()!!
                val timeFormatted = dto.startTime?.take(5) ?: ""
                _uiState.value = EditEventUiState(
                    eventId = dto.id,
                    title = dto.name ?: "",
                    description = dto.description ?: "",
                    address = dto.address ?: "",
                    date = dto.startDate ?: "",
                    time = timeFormatted,

                    // Сохраняем оригинальные значения для сравнения
                    originalTitle = dto.name ?: "",
                    originalDescription = dto.description ?: "",
                    originalAddress = dto.address ?: "",
                    originalDate = dto.startDate ?: "",
                    originalTime = timeFormatted,

                    isLoading = false
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Не удалось загрузить событие"
                )
            }
        }
    }
}

class EditEventViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditEventViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EditEventViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

data class EditEventUiState(
    val eventId: String? = null,
    val title: String = "",
    val description: String = "",
    val address: String = "",
    val date: String = "",
    val time: String = "",

    // Оригинальные значения для сравнения
    val originalTitle: String = "",
    val originalDescription: String = "",
    val originalAddress: String = "",
    val originalDate: String = "",
    val originalTime: String = "",

    val isLoading: Boolean = false,
    val error: String? = null
) {
    val isDirty: Boolean
        get() = title != originalTitle ||
                description != originalDescription ||
                address != originalAddress ||
                date != originalDate ||
                time != originalTime
}