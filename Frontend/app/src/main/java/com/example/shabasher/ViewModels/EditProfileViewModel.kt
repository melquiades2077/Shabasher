package com.example.shabasher.ViewModels

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shabasher.data.dto.UpdateUserProfileRequest
import com.example.shabasher.data.local.TokenManager
import com.example.shabasher.data.network.ProfileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditProfileViewModel(
    private val tokenManager: TokenManager
) : ViewModel() {

    private val profileRepo = ProfileRepository(tokenManager)

    data class UiState(
        val isLoading: Boolean = false,
        val name: String = "",
        val aboutMe: String = "",
        val telegram: String = "",
        val avatarUrl: String? = null,
        val originalName: String = "",
        val originalAboutMe: String = "",
        val originalTelegram: String = "",
        val isAvatarBusy: Boolean = false,
        val error: String? = null
    ) {
        val isDirty: Boolean
            get() = name != originalName ||
                    aboutMe != originalAboutMe ||
                    telegram != originalTelegram
    }

    // ИЗМЕНЕНИЕ 1: Используйте MutableStateFlow для Compose
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun loadCurrentUser() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = profileRepo.getProfile()
            if (result.isSuccess) {
                val user = result.getOrNull()!!
                val name = user.name
                val aboutMe = user.aboutMe ?: ""
                val telegram = user.telegram ?: ""

                _uiState.value = UiState(
                    name = name,
                    aboutMe = aboutMe,
                    telegram = telegram,
                    avatarUrl = user.avatarUrl,
                    originalName = name,
                    originalAboutMe = aboutMe,
                    originalTelegram = telegram
                )
            } else {
                _uiState.update {
                    it.copy(
                        error = "Не удалось загрузить профиль",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun updateAboutMe(aboutMe: String) {
        _uiState.update { it.copy(aboutMe = aboutMe) }
    }

    fun updateTelegram(telegram: String) {
        _uiState.update { it.copy(telegram = telegram) }
    }

    fun uploadAvatar(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAvatarBusy = true, error = null) }

            val payload = withContext(Dispatchers.IO) {
                runCatching {
                    val resolver: ContentResolver = context.contentResolver
                    val type = resolver.getType(uri) ?: "image/jpeg"
                    val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
                        ?: error("Не удалось прочитать файл")
                    Triple(bytes, fileNameFromUri(resolver, uri, type), type)
                }
            }

            val (bytes, fileName, type) = payload.getOrElse {
                _uiState.update { st ->
                    st.copy(
                        isAvatarBusy = false,
                        error = it.message ?: "Не удалось прочитать файл"
                    )
                }
                return@launch
            }

            val result = profileRepo.uploadAvatar(bytes, fileName, type)
            if (result.isSuccess) {
                val updated = result.getOrNull()!!
                _uiState.update {
                    it.copy(
                        isAvatarBusy = false,
                        avatarUrl = updated.avatarUrl,
                        error = null
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isAvatarBusy = false,
                        error = result.exceptionOrNull()?.message ?: "Ошибка загрузки"
                    )
                }
            }
        }
    }

    fun deleteAvatar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isAvatarBusy = true, error = null) }
            val result = profileRepo.deleteAvatar()
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(isAvatarBusy = false, avatarUrl = null, error = null)
                }
            } else {
                _uiState.update {
                    it.copy(
                        isAvatarBusy = false,
                        error = result.exceptionOrNull()?.message ?: "Ошибка удаления"
                    )
                }
            }
        }
    }

    fun save() {
        val current = _uiState.value
        if (!current.isDirty) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val request = UpdateUserProfileRequest(
                name = current.name,
                aboutMe = if (current.aboutMe.isNotBlank()) current.aboutMe else "",
                telegram = current.telegram.ifBlank { null }
            )

            val result = profileRepo.updateProfile(request)
            if (result.isSuccess) {
                val updated = result.getOrNull()!!
                _uiState.value = UiState(
                    name = updated.name,
                    aboutMe = updated.aboutMe ?: "",
                    telegram = updated.telegram ?: "",
                    avatarUrl = updated.avatarUrl,
                    originalName = updated.name,
                    originalAboutMe = updated.aboutMe ?: "",
                    originalTelegram = updated.telegram ?: ""
                )
            } else {
                _uiState.update {
                    current.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Ошибка сохранения"
                    )
                }
            }
        }
    }
}

private fun fileNameFromUri(resolver: ContentResolver, uri: Uri, mimeType: String): String {
    val ext = when (mimeType) {
        "image/png" -> "png"
        "image/webp" -> "webp"
        else -> "jpg"
    }
    val raw = uri.lastPathSegment?.substringAfterLast('/') ?: "avatar"
    val cleaned = raw.replace("[^A-Za-z0-9_.-]".toRegex(), "_").take(40)
    return if (cleaned.contains('.')) cleaned else "$cleaned.$ext"
}
