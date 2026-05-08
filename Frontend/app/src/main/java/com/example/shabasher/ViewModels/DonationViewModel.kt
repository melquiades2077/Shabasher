package com.example.shabasher.ViewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.shabasher.Model.NotificationCategory
import com.example.shabasher.Model.Routes
import com.example.shabasher.Model.UserRole
import com.example.shabasher.data.dto.Fundraise
import com.example.shabasher.data.dto.FundraiseParticipantStatus
import com.example.shabasher.data.dto.FundStatus
import com.example.shabasher.data.dto.GetEventResponse
import com.example.shabasher.data.network.EventsRepository
import com.example.shabasher.data.network.FundraiseLocalState
import com.example.shabasher.data.network.FundraisesRepository
import com.example.shabasher.notifications.FundraiseSnapshots
import com.example.shabasher.notifications.NotificationCenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.math.BigDecimal
import java.time.Instant
import kotlin.coroutines.cancellation.CancellationException

// ═══════════════════════════════════════════════════════
// UI State
// ═══════════════════════════════════════════════════════
sealed interface DonationUiState {
    data object Loading : DonationUiState

    data class Success(
        val donation: Fundraise,
        val currentUserRole: UserRole = UserRole.MEMBER,
        val participantNames: Map<String, String> = emptyMap(),
        val isRefreshing: Boolean = false
    ) : DonationUiState

    data class Error(val message: String, val retry: () -> Unit = {}) : DonationUiState
}

fun UserRole.canManageFundraise(): Boolean =
    this == UserRole.ADMIN || this == UserRole.MODERATOR

data class DonationActionState(
    val isLoading: Boolean = false,
    val pendingParticipantId: String? = null,
    val error: String? = null,
    val success: String? = null,
    /** Сбор удалён успешно — экрану нужно сделать popBackStack. */
    val deleted: Boolean = false
)

// ═══════════════════════════════════════════════════════
// Application-wide scope для критических HTTP-операций.
// Переживает смерть ViewModel — запрос всё равно дойдёт до сервера.
// ═══════════════════════════════════════════════════════
object FundraiseActionScope {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
}

// ═══════════════════════════════════════════════════════
// ViewModel
// ═══════════════════════════════════════════════════════
class DonationViewModel(
    private val repository: FundraisesRepository,
    private val eventsRepository: EventsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DonationUiState>(DonationUiState.Loading)
    val uiState: StateFlow<DonationUiState> = _uiState.asStateFlow()

    private val _actionState = MutableStateFlow(DonationActionState())
    val actionState: StateFlow<DonationActionState> = _actionState.asStateFlow()

    private var currentFundraiseId: String? = null
    private var cachedEventContext: EventContext? = null

    fun loadDonationById(fundraiseId: String) {
        currentFundraiseId = fundraiseId
        viewModelScope.launch {
            _uiState.value = DonationUiState.Loading
            loadDonationInternal(fundraiseId, withFullEventReload = true)
        }
    }

    fun refresh() {
        val fundraiseId = currentFundraiseId ?: return
        val current = _uiState.value as? DonationUiState.Success
            ?: return run { loadDonationById(fundraiseId) }
        _uiState.value = current.copy(isRefreshing = true)
        viewModelScope.launch {
            loadDonationInternal(fundraiseId, withFullEventReload = true)
            _uiState.update {
                if (it is DonationUiState.Success) it.copy(isRefreshing = false) else it
            }
        }
    }

    private suspend fun loadDonationInternal(fundraiseId: String, withFullEventReload: Boolean) {
        val donationResult = try {
            withContext(NonCancellable) { repository.getFundraiseDetails(fundraiseId) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }

        val rawDonation = donationResult.getOrElse { error ->
            Log.e(TAG, "Failed to load fundraise", error)
            val previous = _uiState.value
            if (previous is DonationUiState.Success) {
                _actionState.update { it.copy(error = mapLoadError(error)) }
            } else {
                _uiState.value = DonationUiState.Error(
                    message = mapLoadError(error),
                    retry = { loadDonationById(fundraiseId) }
                )
            }
            return
        }

        // 🔥 Применяем локальные оверрайды (compensate за бэкенд-несогласованности)
        val withOverrides = FundraiseLocalState.apply(rawDonation, repository.getCurrentUserId())

        val ctx = if (withFullEventReload || cachedEventContext?.shabashId != withOverrides.shabashId) {
            resolveEventContext(withOverrides.shabashId, withOverrides.isCreator)
                .also { cachedEventContext = it }
        } else {
            cachedEventContext!!
        }

        // ▶ Если админ — добавляем в список ВСЕХ участников события (даже тех,
        // кто ещё не отметил оплату), чтобы их можно было видеть и confirm'ить.
        val donation = if (ctx.role.canManageFundraise()) {
            mergeWithEventParticipants(withOverrides, ctx.allEventParticipantIds)
        } else {
            withOverrides
        }

        // 🔔 Детект внешних изменений → уведомления
        emitNotificationsForDiff(donation)

        _uiState.value = DonationUiState.Success(
            donation = donation,
            currentUserRole = ctx.role,
            participantNames = ctx.participantNames,
            isRefreshing = (_uiState.value as? DonationUiState.Success)?.isRefreshing ?: false
        )
    }

    /**
     * Добавляет к `donation.participants` всех участников события, которых
     * сервер не вернул (например, они ещё не отметили оплату). Они появятся
     * в списке со статусом NotPaid — без кнопки «Принять», просто видны.
     */
    private fun mergeWithEventParticipants(
        donation: Fundraise,
        eventParticipantIds: List<String>
    ): Fundraise {
        if (eventParticipantIds.isEmpty()) return donation
        val existing = donation.participants.orEmpty()
        val existingIds = existing.map { it.userId }.toSet()
        val missing = eventParticipantIds
            .filter { it !in existingIds }
            .map { uid ->
                com.example.shabasher.data.dto.FundraiseParticipant(
                    userId = uid,
                    status = FundraiseParticipantStatus.NotPaid,
                    amount = java.math.BigDecimal.ZERO,
                    paidAt = null,
                    checkedAt = null
                )
            }
        if (missing.isEmpty()) return donation
        val merged = existing + missing
        return donation.copy(
            participants = merged,
            participantsCount = merged.size
        )
    }

    // ═══════════════ Действия ═══════════════

    fun markPaid() {
        val fundraiseId = currentFundraiseId ?: return
        val current = _uiState.value as? DonationUiState.Success ?: return
        val previousDonation = current.donation
        val currentUserId = repository.getCurrentUserId()

        // Optimistic — сразу Pending
        _uiState.value = current.copy(
            donation = previousDonation.copy(myPaymentStatus = FundraiseParticipantStatus.Pending)
        )
        _actionState.update { it.copy(isLoading = true, error = null, success = null) }

        FundraiseActionScope.scope.launch {
            val result = repository.markPaid(fundraiseId)
            postUiUpdate {
                result
                    .onSuccess {
                        // Сохраняем в локальный кеш — переживёт реалоад экрана.
                        // Привязываем к userId чтобы override не «протекал» на других юзеров.
                        if (currentUserId != null) {
                            FundraiseLocalState.setMyPaymentStatus(
                                fundraiseId, currentUserId, FundraiseParticipantStatus.Pending
                            )
                            FundraiseLocalState.setParticipantStatus(
                                fundraiseId, currentUserId, FundraiseParticipantStatus.Pending
                            )
                        }
                        _actionState.update {
                            it.copy(isLoading = false, success = "Готово, ждите подтверждения от организатора")
                        }
                    }
                    .onFailure { error ->
                        Log.e(TAG, "markPaid failed", error)
                        when {
                            error is IllegalStateException -> {
                                // 409 — уже отмечено, оставляем optimistic
                                if (currentUserId != null) {
                                    FundraiseLocalState.setMyPaymentStatus(
                                        fundraiseId, currentUserId, FundraiseParticipantStatus.Pending
                                    )
                                }
                                _actionState.update {
                                    it.copy(isLoading = false, success = "Оплата уже отправлена на проверку")
                                }
                            }
                            error.message?.contains("закрыт", ignoreCase = true) == true -> {
                                // Бэкенд: «сбор закрыт». Это значит он закрыт независимо
                                // от того что говорит GET — фиксируем локально.
                                FundraiseLocalState.setFundStatus(fundraiseId, FundStatus.Closed)
                                _uiState.update { state ->
                                    if (state is DonationUiState.Success) state.copy(
                                        donation = state.donation.copy(
                                            fundStatus = FundStatus.Closed,
                                            myPaymentStatus = previousDonation.myPaymentStatus
                                        )
                                    ) else state
                                }
                                _actionState.update {
                                    it.copy(isLoading = false, error = "Сбор уже закрыт")
                                }
                            }
                            else -> {
                                _uiState.update { state ->
                                    if (state is DonationUiState.Success) state.copy(donation = previousDonation) else state
                                }
                                _actionState.update {
                                    it.copy(isLoading = false, error = mapActionError(error))
                                }
                            }
                        }
                    }
            }
        }
    }

    fun confirmPayment(participantUserId: String, amount: BigDecimal?) {
        val fundraiseId = currentFundraiseId ?: return
        val current = _uiState.value as? DonationUiState.Success ?: return
        val donation = current.donation
        val currentUserId = repository.getCurrentUserId()
        // Если участника нет в списке (мы потеряли его, например при reload),
        // всё равно отправляем запрос — admin кликнул осознанно.
        val target = donation.participants?.firstOrNull { it.userId == participantUserId }

        val confirmedAmount = amount ?: target?.amount ?: BigDecimal.ZERO
        val wasConfirmed = target?.isConfirmed == true
        val previousAmount = if (wasConfirmed) target!!.amount else BigDecimal.ZERO

        // Optimistic
        val updatedParticipants = (donation.participants ?: emptyList()).let { list ->
            if (list.any { it.userId == participantUserId }) {
                list.map { p ->
                    if (p.userId == participantUserId) p.copy(
                        status = FundraiseParticipantStatus.Confirmed,
                        amount = confirmedAmount,
                        checkedAt = Instant.now()
                    ) else p
                }
            } else {
                // Участника не было в списке — добавляем его
                list + com.example.shabasher.data.dto.FundraiseParticipant(
                    userId = participantUserId,
                    status = FundraiseParticipantStatus.Confirmed,
                    amount = confirmedAmount,
                    paidAt = Instant.now(),
                    checkedAt = Instant.now()
                )
            }
        }
        val deltaConfirmedCount = if (wasConfirmed) 0 else 1
        val deltaAmount = confirmedAmount - previousAmount

        _uiState.value = current.copy(
            donation = donation.copy(
                participants = updatedParticipants,
                confirmedCount = (donation.confirmedCount ?: 0) + deltaConfirmedCount,
                currentAmount = (donation.currentAmount + deltaAmount).coerceAtLeast(BigDecimal.ZERO),
                // Если admin принимает свою же оплату — обновляем myPaymentStatus тоже
                myPaymentStatus = if (participantUserId == currentUserId)
                    FundraiseParticipantStatus.Confirmed else donation.myPaymentStatus
            )
        )
        _actionState.update {
            it.copy(isLoading = true, pendingParticipantId = participantUserId, error = null, success = null)
        }

        FundraiseActionScope.scope.launch {
            val result = repository.confirmPayment(fundraiseId, participantUserId, amount)
            postUiUpdate {
                result
                    .onSuccess {
                        recordConfirmedOverride(fundraiseId, participantUserId, confirmedAmount, currentUserId)
                        _actionState.update {
                            it.copy(isLoading = false, pendingParticipantId = null, success = "Оплата подтверждена")
                        }
                    }
                    .onFailure { error ->
                        Log.e(TAG, "confirmPayment failed", error)
                        if (error is IllegalStateException) {
                            recordConfirmedOverride(fundraiseId, participantUserId, confirmedAmount, currentUserId)
                            _actionState.update {
                                it.copy(isLoading = false, pendingParticipantId = null, success = "Оплата уже была подтверждена")
                            }
                        } else {
                            _uiState.update { state ->
                                if (state is DonationUiState.Success) state.copy(donation = donation) else state
                            }
                            _actionState.update {
                                it.copy(isLoading = false, pendingParticipantId = null, error = mapActionError(error))
                            }
                        }
                    }
            }
        }
    }

    private fun recordConfirmedOverride(
        fundraiseId: String,
        participantUserId: String,
        amount: BigDecimal,
        currentUserId: String?
    ) {
        FundraiseLocalState.setParticipantStatus(
            fundraiseId, participantUserId,
            FundraiseParticipantStatus.Confirmed,
            amount = amount,
            checkedAt = Instant.now()
        )
        // Если admin подтвердил свою же оплату — синхронизируем флаг myPaymentStatus
        if (currentUserId != null && participantUserId == currentUserId) {
            FundraiseLocalState.setMyPaymentStatus(
                fundraiseId, currentUserId, FundraiseParticipantStatus.Confirmed
            )
        }
    }

    fun revertPayment(participantUserId: String) {
        val fundraiseId = currentFundraiseId ?: return
        val current = _uiState.value as? DonationUiState.Success ?: return
        val donation = current.donation
        val currentUserId = repository.getCurrentUserId()
        val target = donation.participants?.firstOrNull { it.userId == participantUserId } ?: return
        if (!target.isConfirmed) return

        val updatedParticipants = donation.participants.map { p ->
            if (p.userId == participantUserId) p.copy(
                status = FundraiseParticipantStatus.Pending,
                checkedAt = null
            ) else p
        }

        _uiState.value = current.copy(
            donation = donation.copy(
                participants = updatedParticipants,
                confirmedCount = ((donation.confirmedCount ?: 1) - 1).coerceAtLeast(0),
                currentAmount = (donation.currentAmount - target.amount).coerceAtLeast(BigDecimal.ZERO),
                // Если admin отменяет свою же оплату — myPaymentStatus тоже на Pending
                myPaymentStatus = if (participantUserId == currentUserId)
                    FundraiseParticipantStatus.Pending else donation.myPaymentStatus
            )
        )
        _actionState.update {
            it.copy(isLoading = true, pendingParticipantId = participantUserId, error = null, success = null)
        }

        FundraiseActionScope.scope.launch {
            val result = repository.revertPayment(fundraiseId, participantUserId)
            postUiUpdate {
                result
                    .onSuccess {
                        recordRevertedOverride(fundraiseId, participantUserId, currentUserId)
                        _actionState.update {
                            it.copy(isLoading = false, pendingParticipantId = null, success = "Подтверждение отменено")
                        }
                    }
                    .onFailure { error ->
                        Log.e(TAG, "revertPayment failed", error)
                        if (error is IllegalStateException) {
                            recordRevertedOverride(fundraiseId, participantUserId, currentUserId)
                            _actionState.update {
                                it.copy(isLoading = false, pendingParticipantId = null, success = "Подтверждение уже было отменено")
                            }
                        } else {
                            _uiState.update { state ->
                                if (state is DonationUiState.Success) state.copy(donation = donation) else state
                            }
                            _actionState.update {
                                it.copy(isLoading = false, pendingParticipantId = null, error = mapActionError(error))
                            }
                        }
                    }
            }
        }
    }

    private fun recordRevertedOverride(
        fundraiseId: String,
        participantUserId: String,
        currentUserId: String?
    ) {
        FundraiseLocalState.setParticipantStatus(
            fundraiseId, participantUserId,
            FundraiseParticipantStatus.Pending
        )
        if (currentUserId != null && participantUserId == currentUserId) {
            FundraiseLocalState.setMyPaymentStatus(
                fundraiseId, currentUserId, FundraiseParticipantStatus.Pending
            )
        }
    }

    fun closeFundraise() {
        val fundraiseId = currentFundraiseId ?: return
        val current = _uiState.value as? DonationUiState.Success ?: return
        val previousDonation = current.donation

        _uiState.value = current.copy(donation = previousDonation.copy(fundStatus = FundStatus.Closed))
        _actionState.update { it.copy(isLoading = true, error = null, success = null) }

        FundraiseActionScope.scope.launch {
            val result = repository.closeFundraise(fundraiseId)
            postUiUpdate {
                result
                    .onSuccess {
                        FundraiseLocalState.setFundStatus(fundraiseId, FundStatus.Closed)
                        _actionState.update { it.copy(isLoading = false, success = "Сбор закрыт") }
                    }
                    .onFailure { error ->
                        Log.e(TAG, "closeFundraise failed", error)
                        if (error is IllegalStateException) {
                            FundraiseLocalState.setFundStatus(fundraiseId, FundStatus.Closed)
                            _actionState.update { it.copy(isLoading = false, success = "Сбор уже закрыт") }
                        } else {
                            _uiState.update { state ->
                                if (state is DonationUiState.Success) state.copy(donation = previousDonation) else state
                            }
                            _actionState.update { it.copy(isLoading = false, error = mapActionError(error)) }
                        }
                    }
            }
        }
    }

    /** Удаление сбора. */
    fun deleteFundraise() {
        val fundraiseId = currentFundraiseId ?: return
        _actionState.update { it.copy(isLoading = true, error = null, success = null) }

        FundraiseActionScope.scope.launch {
            val result = repository.deleteFundraise(fundraiseId)
            postUiUpdate {
                result
                    .onSuccess {
                        // Чистим локальный кеш для этого сбора
                        FundraiseLocalState.clear(fundraiseId)
                        _actionState.update {
                            it.copy(isLoading = false, success = "Сбор удалён", deleted = true)
                        }
                    }
                    .onFailure { error ->
                        Log.e(TAG, "deleteFundraise failed", error)
                        _actionState.update {
                            it.copy(isLoading = false, error = mapActionError(error))
                        }
                    }
            }
        }
    }

    /**
     * Запускает блок в viewModelScope (главный поток). Если ВМ уже мертва —
     * молча игнорируем (запрос-то прошёл, серверное состояние верное).
     */
    private fun postUiUpdate(block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
            } catch (_: CancellationException) {
                // VM умерла — запрос ушёл, override записан в FundraiseLocalState,
                // при следующем заходе всё подтянется.
            }
        }
    }

    /** Сравнивает с прошлым снэпшотом и эмитит уведомления о внешних изменениях. */
    private fun emitNotificationsForDiff(donation: Fundraise) {
        val diff = FundraiseSnapshots.diff(
            donation.id,
            donation.fundStatus,
            donation.myPaymentStatus
        ) ?: return // нет предыдущего снэпшота — первая загрузка, не уведомляем

        val route = Routes.donation(donation.id)

        // Подтверждение моей оплаты (Pending → Confirmed)
        if (diff.myPaymentChanged
            && diff.previousMyPaymentStatus == FundraiseParticipantStatus.Pending
            && donation.myPaymentStatus == FundraiseParticipantStatus.Confirmed
        ) {
            NotificationCenter.notify(
                category = NotificationCategory.Fundraise,
                title = "Оплата подтверждена",
                body = "Организатор подтвердил вашу оплату в сборе «${donation.title}».",
                targetRoute = route,
                sourceKey = "confirm:${donation.id}"
            )
        }

        // Откат моей оплаты (Confirmed → Pending/NotPaid/Reverted)
        if (diff.myPaymentChanged
            && diff.previousMyPaymentStatus == FundraiseParticipantStatus.Confirmed
            && donation.myPaymentStatus != FundraiseParticipantStatus.Confirmed
        ) {
            NotificationCenter.notify(
                category = NotificationCategory.Fundraise,
                title = "Подтверждение оплаты отменено",
                body = "Организатор отменил подтверждение вашей оплаты в сборе «${donation.title}». Возможно, потребуется повторная оплата.",
                targetRoute = route,
                sourceKey = "revert:${donation.id}"
            )
        }

        // Сбор закрыт извне (Active → Closed/Completed)
        if (diff.fundStatusChanged
            && diff.previousFundStatus == FundStatus.Active
            && (donation.fundStatus == FundStatus.Closed || donation.fundStatus == FundStatus.Completed)
        ) {
            val verb = if (donation.fundStatus == FundStatus.Closed) "закрыт" else "завершён"
            NotificationCenter.notify(
                category = NotificationCategory.Fundraise,
                title = "Сбор $verb",
                body = "Сбор «${donation.title}» $verb. Новые оплаты не принимаются.",
                targetRoute = route,
                sourceKey = "close:${donation.id}"
            )
        }
    }

    private suspend fun resolveEventContext(shabashId: String, isCreator: Boolean): EventContext = try {
        coroutineScope {
            val event: GetEventResponse? = withContext(NonCancellable) {
                eventsRepository.getEventById(shabashId).getOrNull()
            }
            val currentUserId = withContext(NonCancellable) { eventsRepository.getCurrentUserId() }

            val role = when {
                event == null || currentUserId == null -> if (isCreator) UserRole.ADMIN else UserRole.MEMBER
                else -> when (event.participants.firstOrNull { it.user.id == currentUserId }?.role) {
                    "Admin" -> UserRole.ADMIN
                    "CoAdmin", "Moderator" -> UserRole.MODERATOR
                    else -> if (isCreator) UserRole.ADMIN else UserRole.MEMBER
                }
            }

            val names = event?.participants
                ?.associate { it.user.id to it.user.name.ifBlank { "Участник" } }
                ?: emptyMap()

            // ВСЕ участники события — чтобы админ видел всех, кого можно подтверждать
            val allEventParticipantIds = event?.participants?.map { it.user.id } ?: emptyList()

            EventContext(shabashId, role, names, allEventParticipantIds)
        }
    } catch (e: Exception) {
        Log.w(TAG, "Не удалось определить роль/имена, fallback", e)
        EventContext(
            shabashId = shabashId,
            role = if (isCreator) UserRole.ADMIN else UserRole.MEMBER,
            participantNames = emptyMap(),
            allEventParticipantIds = emptyList()
        )
    }

    private fun mapLoadError(error: Throwable): String = when (error) {
        is SecurityException -> "Нет доступа к сбору"
        is NoSuchElementException -> "Сбор не найден"
        is IOException -> "Нет связи с сервером. Проверьте интернет"
        else -> when {
            error.message?.contains("connection", ignoreCase = true) == true -> "Нет связи с сервером"
            error.message?.contains("timeout", ignoreCase = true) == true -> "Сервер не отвечает"
            error.message?.contains("UnresolvedAddress", ignoreCase = true) == true -> "Нет связи с сервером"
            else -> error.message ?: "Не удалось загрузить сбор"
        }
    }

    private fun mapActionError(error: Throwable): String = when (error) {
        is SecurityException -> error.message ?: "Нет прав"
        is NoSuchElementException -> error.message ?: "Не найдено"
        is IllegalStateException -> error.message ?: "Действие уже выполнено"
        is IllegalArgumentException -> error.message ?: "Неверные данные"
        is IOException -> "Нет связи с сервером"
        else -> when {
            error.message?.contains("UnresolvedAddress", ignoreCase = true) == true -> "Нет связи с сервером"
            else -> error.message ?: "Ошибка"
        }
    }

    fun clearActionState() {
        _actionState.update { it.copy(error = null, success = null) }
    }

    fun getCurrentUserId(): String? = repository.getCurrentUserId()

    private data class EventContext(
        val shabashId: String,
        val role: UserRole,
        val participantNames: Map<String, String>,
        /** Все юзеры события (включая тех, кто ещё не отметил оплату). */
        val allEventParticipantIds: List<String>
    )

    companion object {
        private const val TAG = "DonationViewModel"
    }
}

class DonationViewModelFactory(
    private val repository: FundraisesRepository,
    private val eventsRepository: EventsRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DonationViewModel::class.java)) {
            return DonationViewModel(repository, eventsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
