package com.example.shabasher.ViewModels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shabasher.Model.EventData
import com.example.shabasher.Model.Participant
import com.example.shabasher.Model.ParticipationStatus
import com.example.shabasher.data.EventsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class EventUiState(
    val isLoading: Boolean = true,
    val event: EventData? = null,
    val error: String? = null
)

class EventViewModel(
    private val repository: EventsRepository = EventsRepository()
) : ViewModel() {

    var ui = androidx.compose.runtime.mutableStateOf(EventUiState())
        private set

    private var eventId: String = ""

    /** Вызывается из экрана */
    fun loadEvent(id: String) {
        eventId = id

        ui.value = ui.value.copy(isLoading = true)

        viewModelScope.launch {
            val result = repository.getEventById(id)

            ui.value = when {
                result.isSuccess -> EventUiState(
                    isLoading = false,
                    event = result.getOrNull()
                )

                else -> EventUiState(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Ошибка загрузки события"
                )
            } as EventUiState
        }
    }

    /** Обновление статуса участия */
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

}
