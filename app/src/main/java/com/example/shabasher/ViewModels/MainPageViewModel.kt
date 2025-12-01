package com.example.shabasher.ViewModels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shabasher.Model.EventShort
import com.example.shabasher.data.EventsRepository
import kotlinx.coroutines.launch

data class MainUiState(
    val events: List<EventShort> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)


class MainPageViewModel(
    private val repository: EventsRepository = EventsRepository()
) : ViewModel() {

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
                uiState.value = MainUiState(events = result.getOrNull()!!)
            } else {
                uiState.value = MainUiState(error = "Не удалось загрузить события")
            }
        }
    }
}
