package com.example.shabasher.ViewModels

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shabasher.Model.EventData
import com.example.shabasher.data.network.EventsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.withContext

class EditEventViewModel(
    context: Context
) : ViewModel() {

    private val repository = EventsRepository(context)
    private val _uiState = mutableStateOf(EditEventUiState())
    val uiState: State<EditEventUiState> = _uiState

    fun loadEventForEdit(event: EventData) {
        _uiState.value = _uiState.value.copy(
            eventId = event.id,
            title = event.title,
            description = event.description,
            address = event.place,
            date = event.date,
            time = event.time,
            avatarUrl = event.avatarUrl,
            isLoading = false
        )
    }

    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
    }

    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    fun updateAddress(address: String) {
        _uiState.value = _uiState.value.copy(address = address)
    }

    fun setDate(date: String) {
        _uiState.value = _uiState.value.copy(date = date)
    }

    fun setTime(hour: Int, minute: Int) {
        val formattedTime = String.format("%02d:%02d", hour, minute)
        _uiState.value = _uiState.value.copy(time = formattedTime)
    }

    fun uploadAvatar(context: Context, uri: Uri) {
        val eventId = _uiState.value.eventId ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAvatarBusy = true, error = null)

            val payload = withContext(Dispatchers.IO) {
                runCatching {
                    val resolver: ContentResolver = context.contentResolver
                    val type = resolver.getType(uri) ?: "image/jpeg"
                    val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
                        ?: error("Не удалось прочитать файл")
                    Triple(bytes, fileNameFromUri(uri, type), type)
                }
            }

            val (bytes, fileName, type) = payload.getOrElse {
                _uiState.value = _uiState.value.copy(
                    isAvatarBusy = false,
                    error = it.message ?: "Не удалось прочитать файл"
                )
                return@launch
            }

            val result = repository.uploadEventAvatar(eventId, bytes, fileName, type)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isAvatarBusy = false,
                    avatarUrl = result.getOrNull()?.avatarUrl,
                    error = null
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isAvatarBusy = false,
                    error = result.exceptionOrNull()?.message ?: "Ошибка загрузки"
                )
            }
        }
    }

    fun deleteAvatar() {
        val eventId = _uiState.value.eventId ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAvatarBusy = true, error = null)
            val result = repository.deleteEventAvatar(eventId)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isAvatarBusy = false,
                    avatarUrl = null,
                    error = null
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isAvatarBusy = false,
                    error = result.exceptionOrNull()?.message ?: "Ошибка удаления"
                )
            }
        }
    }

    fun saveEvent() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        val request = EventsRepository.UpdateEventRequest(
            id = _uiState.value.eventId ?: return,
            name = _uiState.value.title,
            description = _uiState.value.description,
            address = _uiState.value.address,
            startDate = _uiState.value.date,
            startTime = "${_uiState.value.time}:00"
        )

        viewModelScope.launch {
            val result = repository.updateEvent(request)
            if (result.isSuccess) {
                // ✅ ОБНОВЛЯЕМ ОРИГИНАЛЬНЫЕ ЗНАЧЕНИЯ ПОСЛЕ УСПЕШНОГО СОХРАНЕНИЯ
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    originalTitle = _uiState.value.title,
                    originalDescription = _uiState.value.description,
                    originalAddress = _uiState.value.address,
                    originalDate = _uiState.value.date,
                    originalTime = _uiState.value.time
                )
                // Опционально: можно вернуться назад
                // onEventSaved?.invoke()
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Не удалось сохранить изменения: ${result.exceptionOrNull()?.message}"
                )
            }
        }
    }

    fun loadEventById(eventId: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val result = repository.getEventById(eventId)
            if (result.isSuccess && result.getOrNull() != null) {
                val dto = result.getOrNull()!!
                val timeFormatted = dto.startTime?.take(5) ?: ""
                _uiState.value = EditEventUiState(
                    eventId = dto.id,
                    title = dto.name ?: "",
                    description = dto.description ?: "",
                    address = dto.address ?: "",
                    date = dto.startDate ?: "",
                    time = timeFormatted,
                    avatarUrl = dto.avatarUrl,

                    // Сохраняем оригинальные значения для сравнения
                    originalTitle = dto.name ?: "",
                    originalDescription = dto.description ?: "",
                    originalAddress = dto.address ?: "",
                    originalDate = dto.startDate ?: "",
                    originalTime = timeFormatted,

                    isLoading = false
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Не удалось загрузить событие"
                )
            }
        }
    }
}

class EditEventViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditEventViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EditEventViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

data class EditEventUiState(
    val eventId: String? = null,
    val title: String = "",
    val description: String = "",
    val address: String = "",
    val date: String = "",
    val time: String = "",
    val avatarUrl: String? = null,
    val isAvatarBusy: Boolean = false,

    // Оригинальные значения для сравнения
    val originalTitle: String = "",
    val originalDescription: String = "",
    val originalAddress: String = "",
    val originalDate: String = "",
    val originalTime: String = "",

    val isLoading: Boolean = false,
    val error: String? = null
) {
    val isDirty: Boolean
        get() = title != originalTitle ||
                description != originalDescription ||
                address != originalAddress ||
                date != originalDate ||
                time != originalTime
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
