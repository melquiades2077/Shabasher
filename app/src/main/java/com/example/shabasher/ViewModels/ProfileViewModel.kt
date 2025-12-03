package com.example.shabasher.ViewModels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shabasher.data.ProfileRepository
import kotlinx.coroutines.launch

data class ProfileUiState(
    val name: String = "",
    val avatarUrl: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)


class ProfileViewModel(
    private val repository: ProfileRepository = ProfileRepository()
) : ViewModel() {

    var uiState = mutableStateOf(ProfileUiState())
        private set

    init {
        loadProfile()
    }

    /** Загружаем данные пользователя */
    fun loadProfile() {
        viewModelScope.launch {
            uiState.value = uiState.value.copy(isLoading = true)

            val result = repository.getProfile()

            uiState.value = if (result.isSuccess) {
                uiState.value.copy(
                    isLoading = false,
                    name = result.getOrNull()?.name ?: "",
                    avatarUrl = result.getOrNull()?.avatarUrl
                )
            } else {
                uiState.value.copy(
                    isLoading = false,
                    error = "Не удалось загрузить профиль"
                )
            }
        }
    }

    /** Изменение имени */
    fun updateName(newName: String) {
        uiState.value = uiState.value.copy(name = newName)
        // TODO: отправить на backend
    }

    /** Загрузка фото */
    fun updateAvatar(url: String) {
        uiState.value = uiState.value.copy(avatarUrl = url)
        // TODO: отправить на backend
    }

    /** Выход из аккаунта */
    fun logout() {
        repository.logout()
    }
}
