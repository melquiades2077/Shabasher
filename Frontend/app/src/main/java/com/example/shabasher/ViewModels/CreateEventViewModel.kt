package com.example.shabasher.ViewModels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shabasher.data.EventsRepository
import kotlinx.coroutines.launch

data class CreateEventUiState(
    val title: String = "",
    val description: String = "",
    val address: String = "",
    val date: String = "",
    val time: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val successEventId: String? = null
)


class CreateEventViewModel(
    private val repository: EventsRepository = EventsRepository()
) : ViewModel() {

    var uiState = mutableStateOf(CreateEventUiState())
        private set

    fun updateTitle(value: String) {
        uiState.value = uiState.value.copy(title = value)
    }

    fun updateDescription(value: String) {
        uiState.value = uiState.value.copy(description = value)
    }

    fun updateAddress(value: String) {
        uiState.value = uiState.value.copy(address = value)
    }

    fun setDate(value: String) {
        uiState.value = uiState.value.copy(date = value)
    }

    fun setTime(h: Int, m: Int) {
        uiState.value = uiState.value.copy(time = "%02d:%02d".format(h, m))
    }

    fun createEvent() {
        val s = uiState.value

        if (s.title.isBlank() ||
            s.description.isBlank() ||
            s.address.isBlank() ||
            s.date.isBlank() ||
            s.time.isBlank()
        ) {
            uiState.value = s.copy(error = "Заполните все поля")
            return
        }

        viewModelScope.launch {
            uiState.value = s.copy(isLoading = true, error = null)

            val result = repository.createEvent(
                title = s.title,
                description = s.description,
                address = s.address,
                date = s.date,
                time = s.time
            )

            if (result.isSuccess) {
                uiState.value = s.copy(
                    isLoading = false,
                    successEventId = result.getOrNull()
                )
            } else {
                uiState.value = s.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Ошибка"
                )
            }
        }
    }
}



