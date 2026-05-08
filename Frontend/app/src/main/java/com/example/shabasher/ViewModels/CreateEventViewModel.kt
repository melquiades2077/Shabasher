package com.example.shabasher.ViewModels

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shabasher.data.network.EventsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class CreateEventUiState(
    val title: String = "",
    val description: String = "",
    val address: String = "",
    val date: String = "",
    val time: String = "",
    val pendingAvatarUri: Uri? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successEventId: String? = null
)

class CreateEventViewModel(
    context: Context
) : ViewModel() {
    private val repository = EventsRepository(context)
    private val sharedContext = context

    var uiState = mutableStateOf(CreateEventUiState())
        private set

    var onEventCreated: (() -> Unit)? = null

    fun updateTitle(value: String) {
        uiState.value = uiState.value.copy(title = value)
    }

    fun updateDescription(value: String) {
        uiState.value = uiState.value.copy(description = value)
    }

    fun updateAddress(value: String) {
        uiState.value = uiState.value.copy(address = value)
    }

    fun setDate(value: String) {
        uiState.value = uiState.value.copy(date = value)
    }

    fun setTime(h: Int, m: Int) {
        uiState.value = uiState.value.copy(time = "%02d:%02d".format(h, m))
    }

    fun setPendingAvatar(uri: Uri?) {
        uiState.value = uiState.value.copy(pendingAvatarUri = uri)
    }

    fun createEvent() {
        val s = uiState.value

        if (s.title.isBlank() ||
            s.description.isBlank() ||
            s.address.isBlank() ||
            s.date.isBlank() ||
            s.time.isBlank()
        ) {
            uiState.value = s.copy(error = "Заполните все поля")
            return
        }

        viewModelScope.launch {
            uiState.value = s.copy(isLoading = true, error = null)

            val result = repository.createEvent(
                title = s.title.trim(),
                description = s.description.trim(),
                address = s.address.trim(),
                date = s.date,
                time = s.time
            )

            if (result.isSuccess) {
                val newEventId = result.getOrNull() ?: ""

                // Если пользователь выбрал фото при создании — загружаем его до перехода
                val pendingUri = s.pendingAvatarUri
                if (pendingUri != null && newEventId.isNotEmpty()) {
                    runCatching {
                        val payload = withContext(Dispatchers.IO) {
                            val resolver: ContentResolver = sharedContext.contentResolver
                            val type = resolver.getType(pendingUri) ?: "image/jpeg"
                            val bytes = resolver.openInputStream(pendingUri)?.use { it.readBytes() }
                                ?: error("Не удалось прочитать файл")
                            Triple(bytes, fileNameFromUri(pendingUri, type), type)
                        }
                        repository.uploadEventAvatar(newEventId, payload.first, payload.second, payload.third)
                    }
                }

                uiState.value = s.copy(
                    isLoading = false,
                    successEventId = newEventId
                )

                onEventCreated?.invoke()

                saveLastCreatedEventId(newEventId)
            } else {
                uiState.value = s.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Ошибка"
                )
            }
        }
    }

    private fun saveLastCreatedEventId(eventId: String) {
        val sharedPrefs = sharedContext.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit()
            .putString("last_created_event_id", eventId)
            .apply()
    }
}

private fun fileNameFromUri(uri: Uri, mimeType: String): String {
    val ext = when (mimeType) {
        "image/png" -> "png"
        "image/webp" -> "webp"
        else -> "jpg"
    }
    val raw = uri.lastPathSegment?.substringAfterLast('/') ?: "event"
    val cleaned = raw.replace("[^A-Za-z0-9_.-]".toRegex(), "_").take(40)
    return if (cleaned.contains('.')) cleaned else "$cleaned.$ext"
}

