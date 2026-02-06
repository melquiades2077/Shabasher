package com.example.shabasher.ViewModels

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shabasher.Model.EventData
import com.example.shabasher.Model.Participant
import com.example.shabasher.Model.ParticipationStatus
import com.example.shabasher.Model.UserRole
import com.example.shabasher.data.dto.EventParticipantDto
import com.example.shabasher.data.dto.GetEventResponse
import com.example.shabasher.data.network.EventsRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

data class EventUiState(
    val isLoading: Boolean = true,
    val event: EventData? = null,
    val error: String? = null,
    val isUpdatingStatus: Boolean = false,
    val isJoining: Boolean = false
)

class EventViewModel(
    context: Context
) : ViewModel() {
    private val repository = EventsRepository(context)
    var ui = mutableStateOf(EventUiState())
        private set
    private val sharedPrefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    // Чтобы избежать повторного присоединения при пересоздании ViewModel
    private var hasJoinedForEvent = mutableSetOf<String>()

    private val _eventLeaveResult = MutableSharedFlow<Result<Unit>>()
    val eventLeaveResult = _eventLeaveResult.asSharedFlow()

    fun leaveFromEvent(shabashId: String) {
        viewModelScope.launch {
            val result = repository.leaveFromEvent(shabashId)
            _eventLeaveResult.emit(result)
        }
    }

    // В EventViewModel
    suspend fun leaveFromEventSuspend(shabashId: String): Result<Unit> {
        return repository.leaveFromEvent(shabashId)
    }

    private val _currentUserId = mutableStateOf<String?>(null)
    val currentUserId: String? get() = _currentUserId.value

    init {
        viewModelScope.launch {
            _currentUserId.value = repository.getCurrentUserId()
        }
    }

    fun loadEvent(eventId: String) {
        ui.value = ui.value.copy(
            isLoading = true,
            isJoining = false,
            error = null
        )

        viewModelScope.launch {
            // === 1. ПЕРВАЯ ПОПЫТКА ЗАГРУЗКИ ===
            val firstLoad = repository.getEventById(eventId)

            if (firstLoad.isSuccess && firstLoad.getOrNull() != null) {
                // Пользователь уже участник → просто показываем
                showEvent(firstLoad.getOrNull()!!)
                return@launch
            }

            // === 2. ЕСЛИ НЕ УЧАСТНИК → ДЖОЙНИМСЯ ===
            ui.value = ui.value.copy(isJoining = true)

            val joinResult = repository.addParticipant(eventId)

            if (joinResult.isFailure) {
                val msg = joinResult.exceptionOrNull()?.message ?: ""

                // Сервер говорит "уже участвуете" → это ок
                if (!msg.contains("уже", ignoreCase = true)) {
                    ui.value = ui.value.copy(
                        isLoading = false,
                        isJoining = false,
                        error = "Не удалось присоединиться к событию"
                    )
                    return@launch
                }
            }

            // === 3. ВТОРАЯ ЗАГРУЗКА (УЖЕ КАК УЧАСТНИК) ===
            val secondLoad = repository.getEventById(eventId)

            if (secondLoad.isFailure || secondLoad.getOrNull() == null) {
                ui.value = ui.value.copy(
                    isLoading = false,
                    isJoining = false,
                    error = "Не удалось загрузить событие после входа"
                )
                return@launch
            }

            showEvent(secondLoad.getOrNull()!!)
        }
    }

    private fun showEvent(dto: GetEventResponse) {
        val event = convertToEventData(dto)

        ui.value = ui.value.copy(
            isLoading = false,
            isJoining = false,
            event = event,
            error = null
        )
    }


    private fun checkAndJoinIfNeeded(eventId: String, eventData: EventData) {
        viewModelScope.launch {
            val currentUserId = repository.getCurrentUserId()
            if (currentUserId == null) return@launch

            val amIParticipant = eventData.participants.any { it.id == currentUserId }

            if (!amIParticipant) {
                autoJoinIfNotParticipant(eventId, eventData)
            } else {
                // Уже участник — помечаем, чтобы не проверять снова
                hasJoinedForEvent.add(eventId)
            }
        }
    }

    private fun autoJoinIfNotParticipant(eventId: String, eventData: EventData) {
        viewModelScope.launch {
            ui.value = ui.value.copy(isJoining = true)

            val currentUserId = repository.getCurrentUserId()
            if (currentUserId == null) {
                ui.value = ui.value.copy(isJoining = false)
                return@launch
            }

            println("[EventViewModel] Пользователь не в списке — присоединяемся...")
            val joinResult = repository.addParticipant(eventId)

            if (joinResult.isSuccess) {
                println("[EventViewModel] Успешно присоединились. Обновляем локально.")

                // 🔥 Получаем имя пользователя (можно сохранить при входе)
                val userName = sharedPrefs.getString("user_name", "Вы") ?: "Вы"

                // 🔥 Локально добавляем себя как участника
                val updatedParticipants = eventData.participants + Participant(
                    id = currentUserId,
                    name = userName,
                    status = ParticipationStatus.INVITED
                )

                val updatedEvent = eventData.copy(
                    participants = updatedParticipants,
                    userStatus = ParticipationStatus.INVITED
                )

                ui.value = ui.value.copy(
                    event = updatedEvent,
                    isJoining = false
                )
            } else {
                println("[EventViewModel] Ошибка при присоединении: ${joinResult.exceptionOrNull()?.message}")
                ui.value = ui.value.copy(
                    isJoining = false,
                    error = "Не удалось присоединиться к событию"
                )
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


    fun sortParticipants(participants: List<Participant>): List<Participant> {
        return participants.sortedWith(
            compareBy<Participant> { it.role.priority }
                .thenBy { it.status.priority }
                .thenBy { it.name.lowercase() } // опционально, чтобы стабильно
        )
    }


    private fun convertToEventData(dto: GetEventResponse): EventData {
        val currentUserId = currentUserId ?: ""

        val participants = sortParticipants(
            convertParticipants(dto.participants)
        )


        val myRole = participants
            .find { it.id == currentUserId }
            ?.role
            ?: UserRole.MEMBER

        val userStatus = when (dto.currentUserParticipationStatus?.uppercase()) {
            "GOING" -> ParticipationStatus.GOING
            "NOT_GOING" -> ParticipationStatus.NOT_GOING
            "INVITED", "0", null -> ParticipationStatus.INVITED
            else -> ParticipationStatus.INVITED
        }

        return EventData(
            id = dto.id,
            title = dto.name ?: "Без названия",
            description = dto.description ?: "",
            date = dto.startDate ?: "",
            time = dto.startTime?.take(5) ?: "",
            place = dto.address ?: "",
            participants = participants,
            userStatus = userStatus,
            currentUserRole = myRole
        )

    }



    private suspend fun determineUserStatusFromDto(eventDto: GetEventResponse): ParticipationStatus {
        val currentUserId = repository.getCurrentUserId() ?: return ParticipationStatus.INVITED

        // Ищем СЕБЯ в списке участников
        val myParticipation = eventDto.participants.find { it.user.id == currentUserId }

        return if (myParticipation != null) {
            convertStatus(myParticipation.status)
        } else {
            ParticipationStatus.INVITED
        }
    }

    // Обновляем convertStatus чтобы понимать числовые статусы
    private fun convertStatus(status: String): ParticipationStatus {
        return when (status) {
            "1", "GOING" -> ParticipationStatus.GOING
            "2", "NOT_GOING" -> ParticipationStatus.NOT_GOING
            else -> ParticipationStatus.INVITED
        }
    }

    private fun convertParticipants(
        participantDtos: List<EventParticipantDto>
    ): List<Participant> {
        return participantDtos.map { dto ->
            Participant(
                id = dto.user.id,
                name = dto.user.name ?: "Неизвестный",
                status = convertStatus(dto.status),
                role = when (dto.role) {
                    "Admin" -> UserRole.ADMIN
                    "CoAdmin", "Moderator" -> UserRole.MODERATOR
                    else -> UserRole.MEMBER
                }
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

    // В EventViewModel.kt

    fun kickParticipant(shabashId: String, targetUserId: String) {
        viewModelScope.launch {
            val result = repository.kickParticipant(shabashId, targetUserId)
            if (result.isSuccess) {
                // Перезагрузим событие, чтобы обновить список
                loadEvent(shabashId)
            } else {
                // Можно показать ошибку через SharedFlow
                println("Ошибка кика: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    fun makeAdmin(shabashId: String, targetUserId: String) {
        viewModelScope.launch {
            val result = repository.updateParticipantRole(shabashId, targetUserId, UserRole.ADMIN)
            if (result.isSuccess) {
                loadEvent(shabashId)
            } else {
                println("Ошибка назначения админа: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    fun makeModerator(shabashId: String, targetUserId: String) {
        viewModelScope.launch {
            val result = repository.updateParticipantRole(shabashId, targetUserId, UserRole.MODERATOR)
            if (result.isSuccess) {
                loadEvent(shabashId)
            } else {
                println("Ошибка назначения модератора: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    fun revokeRole(shabashId: String, targetUserId: String) {
        // Разжалование → делаем участником
        viewModelScope.launch {
            val result = repository.updateParticipantRole(shabashId, targetUserId, UserRole.MEMBER)
            if (result.isSuccess) {
                loadEvent(shabashId)
            } else {
                println("Ошибка разжалования: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    fun deleteEvent(shabashId: String) {
        viewModelScope.launch {
            val result = repository.deleteEvent(shabashId)
            _eventLeaveResult.emit(result) // Используем тот же поток для обработки результата
        }
    }
}