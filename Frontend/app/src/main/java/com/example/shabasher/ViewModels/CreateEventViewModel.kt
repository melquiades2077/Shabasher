package com.example.shabasher.ViewModels

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shabasher.data.network.EventsRepository
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
    context: Context
) : ViewModel() {
    private val repository = EventsRepository(context)
    private val sharedContext = context

    var uiState = mutableStateOf(CreateEventUiState())
        private set

    var onEventCreated: (() -> Unit)? = null

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

                onEventCreated?.invoke()

                saveLastCreatedEventId(result.getOrNull() ?: "")
            } else {
                uiState.value = s.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Ошибка"
                )
            }
        }
    }

    private fun saveLastCreatedEventId(eventId: String) {
        val sharedPrefs = sharedContext.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit()
            .putString("last_created_event_id", eventId)
            .apply()
    }
}
