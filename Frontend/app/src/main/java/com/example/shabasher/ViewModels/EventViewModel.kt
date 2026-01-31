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
    val error: String? = null,
    val isUpdatingStatus: Boolean = false
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

    fun updateMyParticipationStatus(newStatus: ParticipationStatus) {
        val currentState = ui.value
        val currentEvent = currentState.event ?: return

        // Получаем ID текущего пользователя (лучше кэшировать один раз при старте)
        viewModelScope.launch {
            val currentUserId = repository.getCurrentUserId()
            if (currentUserId == null) {
                // Обработка ошибки: не авторизован
                return@launch
            }

            // === 1. Оптимистичное обновление UI ===
            val updatedParticipants = currentEvent.participants.map { participant ->
                if (participant.id == currentUserId) {
                    participant.copy(status = newStatus)
                } else {
                    participant
                }
            }

            val optimisticEvent = currentEvent.copy(
                userStatus = newStatus,
                participants = updatedParticipants
            )

            // Обновляем состояние с isUpdating = true
            ui.value = currentState.copy(
                event = optimisticEvent,
                isUpdatingStatus = true,
                error = null
            )

            // === 2. Отправляем запрос на сервер ===
            val result = repository.updateParticipationStatus(currentEvent.id, newStatus)

            // === 3. Обрабатываем результат ===
            if (result.isSuccess) {
                // Успех — оставляем оптимистичное состояние
                ui.value = ui.value.copy(isUpdatingStatus = false)
            } else {
                // Ошибка — откатываем
                ui.value = currentState.copy(
                    isUpdatingStatus = false,
                    error = "Не удалось обновить статус: ${result.exceptionOrNull()?.message}"
                )
                // TODO: показать Snackbar через SharedFlow/StateFlow
            }
        }
    }

    //Обновление статуса участника
    fun updateParticipationStatusOnServer(eventId: String, newStatus: ParticipationStatus) {
        if (ui.value.isUpdatingStatus) {
            println("[EventViewModel] Кнопка заблокирована, пропускаем")
            return
        }

        val oldEvent = ui.value.event
        val oldStatus = oldEvent?.userStatus

        ui.value = ui.value.copy(isUpdatingStatus = true)

        viewModelScope.launch {
            val result = repository.updateParticipationStatus(eventId, newStatus)

            ui.value = ui.value.copy(isUpdatingStatus = false)

            if (result.isSuccess) {
                val updatedEvent = ui.value.event?.copy(userStatus = newStatus)
                ui.value = ui.value.copy(event = updatedEvent)
                println("[EventViewModel] Статус обновлен на сервере: $newStatus")
            } else {
                val error = result.exceptionOrNull()?.message ?: "Ошибка обновления статуса"
                println("[EventViewModel] Ошибка: $error")

                if (oldEvent != null && oldStatus != null) {
                    ui.value = ui.value.copy(
                        event = oldEvent.copy(userStatus = oldStatus),
                        error = error
                    )
                }
            }
        }
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

            // Определяем статус текущего пользователя из данных API
            val userStatus = determineUserStatusFromDto(eventDto)

            EventData(
                id = eventDto.id,
                title = eventDto.name ?: "Без названия",
                description = eventDto.description ?: "",
                date = date,
                time = time,
                place = eventDto.address ?: "",
                participants = convertParticipants(eventDto.participants),
                userStatus = userStatus  // Используем реальный статус из API
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Новая функция для определения статуса из DTO
    private fun determineUserStatusFromDto(eventDto: GetEventResponse): ParticipationStatus {
        // Для простоты: если есть участники, берем статус первого
        // В реальности нужно найти текущего пользователя по ID
        return if (eventDto.participants.isNotEmpty()) {
            // Конвертируем числовой статус из API (0,1,2) в наш enum
            val apiStatus = eventDto.participants.first().status
            when (apiStatus) {
                "1" -> ParticipationStatus.GOING
                "2" -> ParticipationStatus.NOT_GOING
                else -> ParticipationStatus.INVITED  // "0" или другой
            }
        } else {
            ParticipationStatus.INVITED
        }
    }

    // Обновляем convertStatus чтобы понимать числовые статусы
    private fun convertStatus(status: String): ParticipationStatus {
        return when (status) {
            "1" -> ParticipationStatus.GOING
            "2" -> ParticipationStatus.NOT_GOING
            "0" -> ParticipationStatus.INVITED
            "GOING" -> ParticipationStatus.GOING
            "NOT_GOING" -> ParticipationStatus.NOT_GOING
            else -> ParticipationStatus.INVITED
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