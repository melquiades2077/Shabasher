package com.example.shabasher.ViewModels

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shabasher.Model.EventShort
import com.example.shabasher.data.network.EventsRepository
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeParseException

data class MainUiState(
    val events: List<EventShort> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val sortOrder: EventSortOrder = EventSortOrder.NEAREST_FIRST
)

enum class EventSortOrder {
    NEAREST_FIRST,   // ближайшие сверху (по возрастанию даты)
    FARTHEST_FIRST   // самые далёкие сверху (по убыванию)
}

class MainPageViewModel(
    context: Context
) : ViewModel() {
    private val repository = EventsRepository(context)

    val uiState = mutableStateOf(MainUiState())

    init {
        loadEvents()
    }

    fun loadEvents() {
        viewModelScope.launch {
            uiState.value = uiState.value.copy(isLoading = true, error = null)

            val result = repository.getEvents()

            if (result.isSuccess) {
                val dtoList = result.getOrNull() ?: emptyList()
                val eventList = dtoList.map { dto ->
                    EventShort(
                        id = dto.id,
                        title = dto.title,
                        date = formatDate(dto.dateTime),
                        status = dto.status
                    )
                }
                // Сохраняем несортированный список, сортируем при отображении
                uiState.value = uiState.value.copy(
                    events = eventList,
                    isLoading = false
                )
            } else {
                uiState.value = uiState.value.copy(
                    isLoading = false,
                    error = "Не удалось загрузить события"
                )
            }
        }
    }

    fun setSortOrder(order: EventSortOrder) {
        uiState.value = uiState.value.copy(sortOrder = order)
    }

    private fun formatDate(dateTime: String): String {
        return try {
            // Предполагаем, что входящий формат — ISO 8601, например: "2026-04-23T10:00:00"
            val localDate = LocalDate.parse(dateTime.take(10)) // берём только YYYY-MM-DD
            localDate.toString() // "2026-04-23"
        } catch (e: DateTimeParseException) {
            // fallback — оставляем как есть или используем заглушку
            dateTime
        }
    }

    // Вспомогательная функция для сортировки (можно вызывать из UI)
    fun getSortedEvents(events: List<EventShort>, order: EventSortOrder): List<EventShort> {
        return try {
            when (order) {
                EventSortOrder.NEAREST_FIRST -> events.sortedBy { parseDate(it.date) }
                EventSortOrder.FARTHEST_FIRST -> events.sortedByDescending { parseDate(it.date) }
            }
        } catch (e: Exception) {
            // Если парсинг сломался — возвращаем как есть
            events
        }
    }

    private fun parseDate(dateStr: String): LocalDate {
        return LocalDate.parse(dateStr)
    }
}