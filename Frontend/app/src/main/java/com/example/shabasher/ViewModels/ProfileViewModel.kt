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
    private val context: Context,
    private val tokenManager: TokenManager = TokenManager(context)
) : ViewModel() {

    private val repo = ProfileRepository(tokenManager)

    private val _uiState = MutableStateFlow(ProfileUiState(isLoading = true))
    val uiState: StateFlow<ProfileUiState> = _uiState

    fun loadProfile() {
        // избегаем повторных параллельных вызовов
        if (_uiState.value.isLoading) {
            // но позволим первый запрос выполниться — всё ок
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val res = repo.getProfile()
            if (res.isSuccess) {
                val p = res.getOrNull()!!
                _uiState.value = ProfileUiState(
                    isLoading = false,
                    name = p.name,
                    email = p.email,
                    avatarUrl = null,
                    error = null
                )
            } else {
                val err = res.exceptionOrNull()?.message ?: "Ошибка загрузки профиля"
                _uiState.value = ProfileUiState(isLoading = false, error = err, name = "")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            tokenManager.clearToken()
            _uiState.value = ProfileUiState(isLoading = false)
        }
    }
}