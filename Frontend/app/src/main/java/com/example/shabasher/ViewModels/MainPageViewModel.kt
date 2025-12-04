package com.example.shabasher.ViewModels

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shabasher.Model.EventShort
import com.example.shabasher.data.network.EventsRepository
import kotlinx.coroutines.launch

data class MainUiState(
    val events: List<EventShort> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class MainPageViewModel(
    context: Context
) : ViewModel() {
    private val repository = EventsRepository(context)

    var uiState = mutableStateOf(MainUiState())
        private set

    init {
        loadEvents()
    }

    fun loadEvents() {
        viewModelScope.launch {
            uiState.value = uiState.value.copy(isLoading = true)

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
                uiState.value = MainUiState(events = eventList)
            } else {
                uiState.value = MainUiState(error = "Не удалось загрузить события")
            }
        }
    }

    private fun formatDate(dateTime: String): String {
        // TODO: реализовать парсинг даты
        return dateTime
    }
}