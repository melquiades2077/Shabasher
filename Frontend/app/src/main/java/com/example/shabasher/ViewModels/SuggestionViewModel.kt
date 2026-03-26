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
            // 1️⃣ Сохраняем текущее состояние для возможного отката
            val originalSuggestions = _suggestions.value

            // 2️⃣ Мгновенно обновляем UI (optimistic update)
            val updatedSuggestions = originalSuggestions.map { suggestion ->
                if (suggestion.id == id) {
                    when (action) {
                        "like" -> {
                            // Переключаем лайк: если уже лайкнуто — убираем, иначе — ставим
                            // При этом снимаем дизлайк, если он был
                            val newLiked = !suggestion.liked
                            val newDisliked = if (newLiked) false else suggestion.disliked
                            val newLikes = if (newLiked) suggestion.likes + 1 else suggestion.likes - 1
                            val newDislikes = if (suggestion.disliked && newDisliked) suggestion.dislikes - 1 else suggestion.dislikes

                            suggestion.copy(
                                likes = newLikes.coerceAtLeast(0),
                                dislikes = newDislikes.coerceAtLeast(0),
                                liked = newLiked,
                                disliked = newDisliked
                            )
                        }
                        "dislike" -> {
                            // Переключаем дизлайк: если уже дизлайкнуто — убираем, иначе — ставим
                            // При этом снимаем лайк, если он был
                            val newDisliked = !suggestion.disliked
                            val newLiked = if (newDisliked) false else suggestion.liked
                            val newDislikes = if (newDisliked) suggestion.dislikes + 1 else suggestion.dislikes - 1
                            val newLikes = if (suggestion.liked && newLiked) suggestion.likes - 1 else suggestion.likes

                            suggestion.copy(
                                likes = newLikes.coerceAtLeast(0),
                                dislikes = newDislikes.coerceAtLeast(0),
                                liked = newLiked,
                                disliked = newDisliked
                            )
                        }
                        else -> suggestion
                    }
                } else suggestion
            }

            // Применяем изменения к UI сразу
            _suggestions.value = updatedSuggestions

            // 3️⃣ Отправляем запрос на сервер
            val result = repository.vote(id, action)

            // 4️⃣ Если сервер вернул ошибку — откатываем изменения назад
            if (result.isFailure) {
                android.util.Log.e("SuggestionsVM", "Vote failed, rolling back: ${result.exceptionOrNull()?.message}")
                _suggestions.value = originalSuggestions
                _error.value = result.exceptionOrNull()?.message?.takeIf { it.isNotBlank() } ?: "Не удалось проголосовать"
            }
            // 5️⃣ Если успех — можно синхронизировать с точными данными с сервера (опционально)
            else {
                result.getOrNull()?.let { serverVote ->
                    android.util.Log.d("SuggestionsVM", "✓ Vote synced: likes=${serverVote.likes}, liked=${serverVote.liked}")
                    // Обновляем финальными значениями с сервера (на случай, если логика на бэке сложнее)
                    _suggestions.value = _suggestions.value.map {
                        if (it.id == id) {
                            it.copy(
                                likes = serverVote.likes,
                                dislikes = serverVote.dislikes,
                                liked = serverVote.liked,
                                disliked = serverVote.disliked
                            )
                        } else it
                    }
                }
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