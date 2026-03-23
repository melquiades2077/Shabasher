package com.example.shabasher.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shabasher.Model.Suggestion
import com.example.shabasher.data.network.FakeSuggestionsRepository
import com.example.shabasher.data.network.SuggestionsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class SuggestionsUiState(
    val suggestions: List<Suggestion> = emptyList(),
    val isLoading: Boolean = false,
    val inputText: String = "",
    val currentUserId: String = "user_1",
    val isAdmin: Boolean = false
)

class SuggestionsViewModel(
    private val repository: SuggestionsRepository = FakeSuggestionsRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SuggestionsUiState())
    val uiState: StateFlow<SuggestionsUiState> = _uiState

    private val eventId = "event_1"

    init {
        loadSuggestions()
    }

    fun loadSuggestions() {
        viewModelScope.launch {

            _uiState.value = _uiState.value.copy(isLoading = true)

            val suggestions = repository.getSuggestions(eventId)

            _uiState.value = _uiState.value.copy(
                suggestions = suggestions,
                isLoading = false
            )
        }
    }

    fun onTextChanged(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }

    fun createSuggestion() {

        val text = _uiState.value.inputText.trim()
        if (text.isEmpty()) return

        viewModelScope.launch {

            val newSuggestion = repository.createSuggestion(eventId, text)

            _uiState.value = _uiState.value.copy(
                suggestions = listOf(newSuggestion) + _uiState.value.suggestions,
                inputText = ""
            )
        }
    }

    fun vote(suggestionId: Int, action: String) {

        viewModelScope.launch {

            val result = repository.vote(suggestionId, action)

            _uiState.value = _uiState.value.copy(
                suggestions = _uiState.value.suggestions.map {

                    if (it.id == suggestionId) {
                        it.copy(
                            likes = result.likes,
                            dislikes = result.dislikes,
                            liked = result.liked,
                            disliked = result.disliked
                        )
                    } else it
                }
            )
        }
    }

    fun deleteSuggestion(suggestionId: Int) {

        viewModelScope.launch {

            repository.deleteSuggestion(suggestionId)

            _uiState.value = _uiState.value.copy(
                suggestions = _uiState.value.suggestions.filter {
                    it.id != suggestionId
                }
            )
        }
    }
}
