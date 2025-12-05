package com.example.shabasher.ViewModels

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shabasher.Model.EventData
import com.example.shabasher.Model.Participant
import com.example.shabasher.Model.ParticipationStatus
import com.example.shabasher.data.dto.EventParticipantDto
import com.example.shabasher.data.dto.GetEventResponse
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

    fun loadEvent(id: String) {
        println("[EventViewModel] Загрузка события с ID: '$id'")

        ui.value = ui.value.copy(isLoading = true)

        viewModelScope.launch {
            val result = repository.getEventById(id)

            ui.value = when {
                result.isSuccess -> {
                    val eventDto = result.getOrNull()
                    println("[EventViewModel] Получен DTO: ${eventDto?.name}")
                    val eventData = convertToEventData(eventDto)
                    if (eventData != null) {
                        println("[EventViewModel] Событие успешно преобразовано: ${eventData.title}")
                        EventUiState(
                            isLoading = false,
                            event = eventData
                        )
                    } else {
                        println("[EventViewModel] Не удалось преобразовать данные события")
                        EventUiState(
                            isLoading = false,
                            error = "Не удалось преобразовать данные события"
                        )
                    }
                }
                else -> {
                    val error = result.exceptionOrNull()?.message ?: "Ошибка загрузки события"
                    println("[EventViewModel] Ошибка: $error")
                    EventUiState(
                        isLoading = false,
                        error = error
                    )
                }
            }
        }
    }

    // Добавим функцию для обновления статуса участника на сервере
    fun updateParticipationStatusOnServer(eventId: String, newStatus: ParticipationStatus) {
        viewModelScope.launch {
            val result = repository.updateParticipationStatus(eventId, newStatus)

            // Обработка результата
            if (result.isSuccess) {
                val updatedEvent = ui.value.event?.copy(userStatus = newStatus)
                ui.value = ui.value.copy(event = updatedEvent)
                println("[EventViewModel] Статус обновлен на сервере: $newStatus")
            } else {
                val error = result.exceptionOrNull()?.message ?: "Ошибка обновления статуса"
                println("[EventViewModel] Ошибка обновления статуса: $error")
                ui.value = ui.value.copy(error = error)
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

        println("[EventViewModel] Статус обновлен: $oldStatus -> $newStatus")
    }

    private fun convertToEventData(eventDto: GetEventResponse?): EventData? {
        if (eventDto == null) return null

        return try {
            val date = eventDto.startDate ?: ""

            val time = if (!eventDto.startTime.isNullOrEmpty()) {
                if (eventDto.startTime.length >= 5) {
                    eventDto.startTime.substring(0, 5)
                } else {
                    eventDto.startTime
                }
            } else {
                ""
            }

            EventData(
                id = eventDto.id,
                title = eventDto.name ?: "Без названия",
                description = eventDto.description ?: "",
                date = date,
                time = time,
                place = eventDto.address ?: "",
                participants = convertParticipants(eventDto.participants),
                userStatus = ParticipationStatus.INVITED
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun convertParticipants(participantDtos: List<EventParticipantDto>): List<Participant> {
        return participantDtos.map { dto ->
            Participant(
                id = dto.user.id,
                name = dto.user.name ?: "Неизвестный",
                status = convertStatus(dto.status)
            )
        }
    }

    private fun convertStatus(status: String): ParticipationStatus {
        return when (status.uppercase()) {
            "GOING" -> ParticipationStatus.GOING
            "NOT_GOING" -> ParticipationStatus.NOT_GOING
            else -> ParticipationStatus.INVITED
        }
    }

    fun addParticipantToEvent(eventId: String) {
        viewModelScope.launch {
            val result = repository.addParticipant(eventId)
            if (result.isSuccess) {
                println("Участник успешно добавлен в событие.")
            } else {
                println("Ошибка при добавлении участника: ${result.exceptionOrNull()?.message}")
            }
        }
    }
}