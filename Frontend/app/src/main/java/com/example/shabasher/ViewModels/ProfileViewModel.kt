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
    val aboutMe: String? = null,       // ← добавлено
    val telegram: String? = null,      // ← добавлено
    val avatarUrl: String? = null,
    val eventsCount: Int = 0,          // ← можно добавить позже
    val organizedCount: Int = 0,
    val participatingCount: Int = 0,
    val error: String? = null
)

class ProfileViewModel(
    private val context: Context,
    private val tokenManager: TokenManager = TokenManager(context),
    private val targetUserId: String? = null // null = мой профиль, иначе — чужой
) : ViewModel() {

    private val repo = ProfileRepository(tokenManager)

    private val _uiState = MutableStateFlow(ProfileUiState(isLoading = true))
    val uiState: StateFlow<ProfileUiState> = _uiState

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val res = if (targetUserId != null) {
                repo.getProfileById(targetUserId)
            } else {
                repo.getProfile()
            }

            if (res.isSuccess) {
                val p = res.getOrNull()!!

                // 🔥 Подсчёт статистики по ролям из participations
                var organizedCount = 0
                var participatingCount = 0

                for (participation in p.participations) {
                    val role = participation.role?.uppercase() ?: "MEMBER"
                    if (role == "ADMIN" || role == "MODERATOR") {
                        organizedCount++
                    } else {
                        participatingCount++
                    }
                }

                _uiState.value = ProfileUiState(
                    isLoading = false,
                    name = p.name,
                    email = p.email,
                    aboutMe = p.aboutMe,
                    telegram = p.telegram,
                    avatarUrl = null, // TODO
                    eventsCount = p.participations.size,
                    organizedCount = organizedCount,
                    participatingCount = participatingCount,
                    error = null
                )
            } else {
                val err = res.exceptionOrNull()?.message ?: "Ошибка загрузки профиля"
                _uiState.value = ProfileUiState(isLoading = false, error = err, name = "")
            }
        }
    }

    // Метод выхода — только для своего профиля
    fun logout() {
        if (targetUserId != null) return // нельзя выйти из чужого профиля

        viewModelScope.launch {
            tokenManager.clearToken()
            // навигация обрабатывается снаружи
        }
    }

    val isOwnProfile: Boolean get() = targetUserId == null
}