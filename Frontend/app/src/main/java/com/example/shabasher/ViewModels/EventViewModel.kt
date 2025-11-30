package com.example.shabasher.ViewModels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shabasher.Model.EventData
import com.example.shabasher.Model.EventUiState
import com.example.shabasher.Model.Participant
import com.example.shabasher.Model.ParticipationStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class EventViewModel : ViewModel() {

    var uiState by mutableStateOf(EventUiState())
        private set

    /**
     * Загружаем данные события.
     * Сейчас — фейковые данные. Позже подменим репозиторием.
     */
    fun loadEvent(eventId: String) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)

            // имитация запроса к серверу
            delay(400)

            val fakeEvent = EventData(
                id = eventId,
                title = "Корпоратив",
                description = "Описание мероприятия. Веселье, игры, конкурсы и хорошее настроение!",
                date = "12 декабря 2026",
                place = "г. Красный Луч, ул. Маяковского 10",
                time = "22:00",
                participants = listOf(
                    Participant("1", "Мария", ParticipationStatus.INVITED),
                    Participant("2", "Степан", ParticipationStatus.INVITED),
                    Participant("3", "Алексей", ParticipationStatus.INVITED)
                ),
                myStatus = ParticipationStatus.GOING
            )

            uiState = uiState.copy(
                isLoading = false,
                event = fakeEvent
            )
        }
    }

    /**
     * Обновление статуса участия.
     * Сейчас — просто подменяем локально.
     * С API — отправишь запрос в репозиторий.
     */
    fun updateStatus(status: ParticipationStatus) {
        uiState.event?.let { current ->

            uiState = uiState.copy(
                event = current.copy(myStatus = status)
            )

            // если будет API — делаешь:
            // repository.updateStatus(current.id, status)
        }
    }
}
