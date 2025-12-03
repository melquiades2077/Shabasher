package com.example.shabasher.ViewModels

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shabasher.data.local.TokenManager
import com.example.shabasher.data.network.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = true,
    val name: String = "",
    val email: String = "",
    val avatarUrl: String? = null,
    val error: String? = null
)

class ProfileViewModel(
    private val context: Context
) : ViewModel() {
    private val repo = ProfileRepository(context)
    private val tokenManager = TokenManager(context)

    var userName = mutableStateOf("Загрузка...")
    var userEmail = mutableStateOf("")
    var loading = mutableStateOf(false)
    var error = mutableStateOf<String?>(null)

    private val _uiState = MutableStateFlow(ProfileUiState(isLoading = true))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        println("[ProfileViewModel] loadProfile() вызван")
        loading.value = true
        error.value = null
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            println("[ProfileViewModel] Запускаем загрузку...")

            repo.getProfile()
                .onSuccess { profile ->
                    println("[ProfileViewModel] УСПЕХ: Получен профиль: ${profile.name}")

                    userName.value = profile.name
                    userEmail.value = profile.email

                    _uiState.value = ProfileUiState(
                        isLoading = false,
                        name = profile.name,
                        email = profile.email,
                        avatarUrl = null
                    )
                    println("[ProfileViewModel] uiState обновлен: name=${profile.name}")
                }
                .onFailure { e ->
                    println("[ProfileViewModel] ОШИБКА: ${e.message}")

                    _uiState.value = ProfileUiState(
                        isLoading = false,
                        error = e.message ?: "Ошибка загрузки",
                        name = "Ошибка"
                    )
                }

            loading.value = false
            println("[ProfileViewModel] Загрузка завершена")
        }
    }

    fun logout() {
        viewModelScope.launch {
            tokenManager.clearToken()
        }
    }
}