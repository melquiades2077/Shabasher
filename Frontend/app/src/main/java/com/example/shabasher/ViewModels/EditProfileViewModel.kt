package com.example.shabasher.ViewModels

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shabasher.data.dto.UpdateUserProfileRequest
import com.example.shabasher.data.local.TokenManager
import com.example.shabasher.data.network.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EditProfileViewModel(
    private val tokenManager: TokenManager
) : ViewModel() {

    private val profileRepo = ProfileRepository(tokenManager)

    data class UiState(
        val isLoading: Boolean = false,
        val name: String = "",
        val aboutMe: String = "",
        val telegram: String = "",
        val originalName: String = "",
        val originalAboutMe: String = "",
        val originalTelegram: String = "",
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