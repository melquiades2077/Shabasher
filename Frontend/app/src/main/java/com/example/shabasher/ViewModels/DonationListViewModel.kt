package com.example.shabasher.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.shabasher.Model.NotificationCategory
import com.example.shabasher.Model.Routes
import com.example.shabasher.Model.UserRole
import com.example.shabasher.data.dto.Fundraise
import com.example.shabasher.data.network.EventsRepository
import com.example.shabasher.data.network.FundraiseLocalState
import com.example.shabasher.data.network.FundraisesRepository
import com.example.shabasher.notifications.FundraiseSnapshots
import com.example.shabasher.notifications.NotificationCenter
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

data class DonationListState(
    val donations: List<Fundraise> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentUserRole: UserRole = UserRole.MEMBER
) {
    val canCreateFundraise: Boolean
        get() = currentUserRole == UserRole.ADMIN || currentUserRole == UserRole.MODERATOR
}

sealed class DonationListEvent {
    data class ShowSnackbar(val message: String) : DonationListEvent()
}

class DonationListViewModel(
    private val repository: FundraisesRepository,
    private val eventsRepository: EventsRepository,
    val eventId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(DonationListState(isLoading = true))
    val uiState: StateFlow<DonationListState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<DonationListEvent>()
    val events = _events.asSharedFlow()

    init {
        loadDonations()
    }

    fun loadDonations() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // Параллельно тянем сборы и событие (чтобы определить роль)
            val (donationsResult, role) = coroutineScope {
                val donations = async { repository.getAllFundraises(eventId) }
                val resolvedRole = async { resolveRole(eventId) }
                donations.await() to resolvedRole.await()
            }

            val currentUserId = eventsRepository.getCurrentUserId()

            donationsResult
                .onSuccess { data ->
                    // Применяем локальные оверрайды чтобы закрытые/оплаченные оставались таковыми
                    val withOverrides = data.map { FundraiseLocalState.applyToListItem(it, currentUserId) }

                    // Детект новых сборов с момента прошлой загрузки
                    val newIds = FundraiseSnapshots.diffList(eventId, withOverrides.map { it.id })
                    withOverrides
                        .filter { it.id in newIds }
                        .forEach { fresh ->
                            NotificationCenter.notify(
                                category = NotificationCategory.Fundraise,
                                title = "Новый сбор",
                                body = "В вашем событии создан новый сбор: «${fresh.title}»",
                                targetRoute = Routes.donation(fresh.id),
                                sourceKey = "new:${fresh.id}"
                            )
                        }

                    _uiState.update {
                        it.copy(
                            donations = withOverrides,
                            isLoading = false,
                            currentUserRole = role
                        )
                    }
                }
                .onFailure { error ->
                    val message = mapError(error)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = message,
                            currentUserRole = role
                        )
                    }
                    _events.emit(DonationListEvent.ShowSnackbar(message))
                }
        }
    }

    private suspend fun resolveRole(shabashId: String): UserRole {
        return try {
            val response = eventsRepository.getEventById(shabashId).getOrNull() ?: return UserRole.MEMBER
            val currentUserId = eventsRepository.getCurrentUserId() ?: return UserRole.MEMBER
            val raw = response.participants
                .firstOrNull { it.user.id == currentUserId }
                ?.role
            when (raw) {
                "Admin" -> UserRole.ADMIN
                "CoAdmin", "Moderator" -> UserRole.MODERATOR
                else -> UserRole.MEMBER
            }
        } catch (_: Exception) {
            UserRole.MEMBER
        }
    }

    private fun mapError(e: Throwable?): String = when (e) {
        is SecurityException -> "Нет доступа"
        is NoSuchElementException -> "Событие не найдено"
        is IllegalArgumentException -> e.message ?: "Неверные данные"
        is IllegalStateException -> e.message ?: "Действие уже выполнено"
        is IOException -> "Нет связи с сервером"
        else -> when {
            e?.message?.contains("connection", ignoreCase = true) == true -> "Нет связи с сервером"
            e?.message?.contains("timeout", ignoreCase = true) == true -> "Сервер не отвечает"
            else -> e?.message ?: "Ошибка"
        }
    }
}

class DonationListViewModelFactory(
    private val repository: FundraisesRepository,
    private val eventsRepository: EventsRepository,
    private val eventId: String
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DonationListViewModel::class.java)) {
            return DonationListViewModel(repository, eventsRepository, eventId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
