package com.example.shabasher.ViewModels

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shabasher.Model.EventData
import com.example.shabasher.Model.Participant
import com.example.shabasher.Model.ParticipationStatus
import com.example.shabasher.data.network.EventsRepository
import kotlinx.coroutines.launch

data class EventUiState(
    val isLoading: Boolean = true,
    val event: EventData? = null,
    val error: String? = null
)

class EventViewModel(
    context: Context
) : ViewModel() {
    private val repository = EventsRepository(context)

    var ui = mutableStateOf(EventUiState())
        private set

    private var eventId: String = ""

    fun loadEvent(id: String) {
        eventId = id

        ui.value = ui.value.copy(isLoading = true)

        viewModelScope.launch {
            val result = repository.getEventById(id)

            ui.value = when {
                result.isSuccess -> {
                    val eventDto = result.getOrNull()
                    val eventData = convertToEventData(eventDto)
                    EventUiState(
                        isLoading = false,
                        event = eventData
                    )
                }
                else -> EventUiState(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Ошибка загрузки события"
                )
            }
        }
    }

    fun updateParticipation(status: ParticipationStatus) {
        val current = ui.value.event ?: return
        val oldStatus = current.userStatus

        val newStatus =
            if (oldStatus == status)
                ParticipationStatus.INVITED
            else
                status

        val updated = current.copy(userStatus = newStatus)

        ui.value = ui.value.copy(event = updated)

        // TODO: отправить на backend
    }

    private fun convertToEventData(eventDto: Any?): EventData? {
        // TODO: реализовать конвертацию когда будет DTO для полного события
        return null // временно возвращаем null
    }
}
