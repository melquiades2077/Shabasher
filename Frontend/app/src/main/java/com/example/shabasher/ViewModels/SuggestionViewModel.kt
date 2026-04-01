package com.example.shabasher.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shabasher.Model.Suggestion
import com.example.shabasher.data.network.SuggestionsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date

class SuggestionsViewModel(
    private val repository: SuggestionsRepository
) : ViewModel() {

    private val _suggestions = MutableStateFlow<List<Suggestion>>(emptyList())
    val suggestions: StateFlow<List<Suggestion>> = _suggestions

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // ✅ Метод для получения ID текущего пользователя (для UI)
    fun getCurrentUserId(): String? = repository.getCurrentUserId()

    fun load(eventId: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            val result = repository.getSuggestions(eventId)

            if (result.isSuccess) {
                _suggestions.value = result.getOrNull() ?: emptyList()
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Ошибка загрузки"
            }
            _loading.value = false
        }
    }

    fun create(eventId: String, text: String, onSent: () -> Unit = {}) {
        viewModelScope.launch {
            val trimmedText = text.trim()
            if (trimmedText.isEmpty()) return@launch

            val tempId = "temp_${System.currentTimeMillis()}"
            val currentUserId = repository.getCurrentUserId()

            val displayName = "Вы"
            val timestamp = java.time.Instant.now().toString()

            val tempSuggestion = Suggestion(
                id = tempId,
                userId = currentUserId ?: "",
                userName = displayName,
                text = trimmedText,
                likes = 0,
                dislikes = 0,
                timestamp = timestamp,
                liked = false,
                disliked = false
            )

            _suggestions.value = listOf(tempSuggestion) + _suggestions.value
            onSent()

            val result = repository.createSuggestion(eventId, trimmedText)

            if (result.isSuccess) {
                result.getOrNull()?.let { serverSuggestion ->
                    _suggestions.value = _suggestions.value.map {
                        if (it.id == tempId) serverSuggestion else it
                    }
                }
            } else {
                val errorMsg = result.exceptionOrNull()?.message
                android.util.Log.e("SuggestionsVM", "Ошибка создания: $errorMsg")
                _suggestions.value = _suggestions.value.filter { it.id != tempId }
                _error.value = errorMsg?.takeIf { it.isNotBlank() } ?: "Не удалось отправить"
            }
        }
    }

    // ✅ ОБНОВЛЁННЫЙ МЕТОД: голосование с мгновенным визуальным откликом
    fun vote(id: String, action: String) {
        if (id.startsWith("temp_")) return

        viewModelScope.launch {
            val original = _suggestions.value

            _suggestions.value = original.map { s ->
                if (s.id != id) return@map s

                when (action) {
                    "like" -> {
                        when {
                            s.liked -> {
                                // убрать лайк
                                s.copy(
                                    liked = false,
                                    likes = (s.likes - 1).coerceAtLeast(0)
                                )
                            }
                            else -> {
                                // поставить лайк и убрать дизлайк
                                s.copy(
                                    liked = true,
                                    disliked = false,
                                    likes = s.likes + 1,
                                    dislikes = if (s.disliked) (s.dislikes - 1).coerceAtLeast(0) else s.dislikes
                                )
                            }
                        }
                    }

                    "dislike" -> {
                        when {
                            s.disliked -> {
                                // убрать дизлайк
                                s.copy(
                                    disliked = false,
                                    dislikes = (s.dislikes - 1).coerceAtLeast(0)
                                )
                            }
                            else -> {
                                // поставить дизлайк и убрать лайк
                                s.copy(
                                    disliked = true,
                                    liked = false,
                                    dislikes = s.dislikes + 1,
                                    likes = if (s.liked) (s.likes - 1).coerceAtLeast(0) else s.likes
                                )
                            }
                        }
                    }

                    else -> s
                }
            }

            val result = repository.vote(id, action)

            if (result.isFailure) {
                _suggestions.value = original
                _error.value = result.exceptionOrNull()?.message ?: "Ошибка голосования"
            }
        }
    }

    // ✅ Удаление предложения с мгновенным обновлением UI
    fun deleteSuggestion(suggestionId: String) {
        viewModelScope.launch {
            val originalSuggestions = _suggestions.value

            // 1️⃣ Оптимистично удаляем из списка
            _suggestions.value = originalSuggestions.filter { it.id != suggestionId }

            // 2️⃣ Отправляем запрос на сервер
            val result = repository.deleteSuggestion(suggestionId)

            // 3️⃣ При ошибке — откатываем изменение
            if (result.isFailure) {
                android.util.Log.e("SuggestionsVM", "Delete failed: ${result.exceptionOrNull()?.message}")
                _suggestions.value = originalSuggestions
                _error.value = result.exceptionOrNull()?.message?.takeIf { it.isNotBlank() } ?: "Не удалось удалить предложение"
            }
        }
    }



    fun clearError() {
        _error.value = null
    }
}