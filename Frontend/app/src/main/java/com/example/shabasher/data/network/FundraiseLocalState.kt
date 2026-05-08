package com.example.shabasher.data.network

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.shabasher.data.dto.FundStatus
import com.example.shabasher.data.dto.Fundraise
import com.example.shabasher.data.dto.FundraiseParticipant
import com.example.shabasher.data.dto.FundraiseParticipantStatus
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import java.math.BigDecimal
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

/**
 * Локальный кеш-оверрайд состояния сборов в памяти процесса + persistence
 * в SharedPreferences.
 *
 * Зачем:
 *  - Бэкенд иногда возвращает несогласованные данные между разными эндпоинтами
 *    (например, GET /fundraises/{id} говорит fundStatus=Active, а POST /mark-paid
 *    отвечает 400 "Сбор закрыт").
 *  - После успешного действия сервер может с задержкой применять изменение.
 *  - Хочется чтобы UI мгновенно отражал решения пользователя и не «откатывался»
 *    при перезаходе/перезапуске приложения.
 *
 * Persistence: fundStatus (closed sentinel) сохраняется навсегда — раз увидев,
 * что сбор закрыт, мы помним об этом до явной очистки кеша.
 */
object FundraiseLocalState {

    private const val TAG = "FundraiseLocalState"
    private const val PREFS_NAME = "fundraise_local_state"
    private const val KEY_FUND_STATUS = "fund_status_overrides"

    private val fundStatusOverrides = ConcurrentHashMap<String, FundStatus>()

    /**
     * Ключ — `${fundraiseId}|${userId}`.
     * Привязка к userId важна: иначе override «Я оплатил» от админа протекает
     * на участника при смене аккаунта на одном устройстве.
     */
    private val myStatusOverrides = ConcurrentHashMap<String, FundraiseParticipantStatus>()
    private val participantOverrides = ConcurrentHashMap<String, MutableMap<String, ParticipantOverride>>()

    private fun myKey(fundraiseId: String, userId: String) = "$fundraiseId|$userId"

    private var prefs: SharedPreferences? = null
    private val json = Json { ignoreUnknownKeys = true }

    fun init(context: Context) {
        if (prefs != null) return
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        loadFundStatusFromDisk()
    }

    data class ParticipantOverride(
        val status: FundraiseParticipantStatus,
        val amount: BigDecimal?,
        val checkedAt: Instant?
    )

    fun setFundStatus(fundraiseId: String, status: FundStatus) {
        fundStatusOverrides[fundraiseId] = status
        saveFundStatusToDisk()
    }

    fun setMyPaymentStatus(fundraiseId: String, userId: String, status: FundraiseParticipantStatus) {
        myStatusOverrides[myKey(fundraiseId, userId)] = status
    }

    fun clearMyPaymentStatus(fundraiseId: String, userId: String) {
        myStatusOverrides.remove(myKey(fundraiseId, userId))
    }

    fun setParticipantStatus(
        fundraiseId: String,
        userId: String,
        status: FundraiseParticipantStatus,
        amount: BigDecimal? = null,
        checkedAt: Instant? = null
    ) {
        participantOverrides
            .getOrPut(fundraiseId) { ConcurrentHashMap() }[userId] =
            ParticipantOverride(status, amount, checkedAt)
    }

    /** Применяет локальные оверрайды поверх серверной модели. */
    fun apply(fundraise: Fundraise, currentUserId: String?): Fundraise {
        val statusOverride = fundStatusOverrides[fundraise.id]
        val myStatusOverride = currentUserId?.let { myStatusOverrides[myKey(fundraise.id, it)] }
        val pOverrides = participantOverrides[fundraise.id].orEmpty()

        // Нормализуем: всегда работаем со списком (пустым если null).
        val baseList = fundraise.participants.orEmpty()

        // 1) Накладываем participant-оверрайды на существующие записи
        val patched = baseList.map { p ->
            val o = pOverrides[p.userId]
            if (o != null) p.copy(
                status = o.status,
                amount = o.amount ?: p.amount,
                checkedAt = o.checkedAt ?: p.checkedAt
            ) else p
        }

        // 2) Добавляем тех из participantOverrides, кого ещё не было
        val knownIds = patched.map { it.userId }.toSet()
        val extras = pOverrides
            .filterKeys { it !in knownIds }
            .map { (uid, o) ->
                FundraiseParticipant(
                    userId = uid,
                    status = o.status,
                    amount = o.amount ?: BigDecimal.ZERO,
                    paidAt = Instant.now(),
                    checkedAt = o.checkedAt
                )
            }
        var participants: List<FundraiseParticipant> = patched + extras

        // 3) Если у нас есть override моего статуса и меня всё ещё нет в списке — добавляем
        if (myStatusOverride != null && currentUserId != null &&
            participants.none { it.userId == currentUserId }
        ) {
            participants = participants + FundraiseParticipant(
                userId = currentUserId,
                status = myStatusOverride,
                amount = BigDecimal.ZERO,
                paidAt = Instant.now(),
                checkedAt = null
            )
        }

        // Если бэк изначально вернул null (не-админу) и нам нечем заполнить,
        // оставляем null чтобы UI скрыл админ-секцию. Если есть хоть кто-то —
        // используем список.
        val finalParticipants: List<FundraiseParticipant>? =
            if (fundraise.participants == null && participants.isEmpty()) null
            else participants

        val confirmedCount = finalParticipants
            ?.count { it.status == FundraiseParticipantStatus.Confirmed }
            ?: fundraise.confirmedCount
        val currentAmount = finalParticipants
            ?.filter { it.status == FundraiseParticipantStatus.Confirmed }
            ?.fold(BigDecimal.ZERO) { acc, p -> acc + p.amount }
            ?: fundraise.currentAmount

        return fundraise.copy(
            fundStatus = statusOverride ?: fundraise.fundStatus,
            myPaymentStatus = myStatusOverride ?: fundraise.myPaymentStatus,
            participants = finalParticipants,
            confirmedCount = confirmedCount,
            currentAmount = currentAmount,
            participantsCount = finalParticipants?.size ?: fundraise.participantsCount
        )
    }

    /** Применяет оверрайды статуса сбора и моего платежа к карточке в списке. */
    fun applyToListItem(fundraise: Fundraise, currentUserId: String?): Fundraise {
        val statusOverride = fundStatusOverrides[fundraise.id]
        val myStatusOverride = currentUserId?.let { myStatusOverrides[myKey(fundraise.id, it)] }
        return fundraise.copy(
            fundStatus = statusOverride ?: fundraise.fundStatus,
            myPaymentStatus = myStatusOverride ?: fundraise.myPaymentStatus
        )
    }

    fun clear(fundraiseId: String) {
        fundStatusOverrides.remove(fundraiseId)
        // Удаляем все ключи myStatusOverrides начинающиеся с этого fundraiseId
        myStatusOverrides.keys.toList()
            .filter { it.startsWith("$fundraiseId|") }
            .forEach { myStatusOverrides.remove(it) }
        participantOverrides.remove(fundraiseId)
        saveFundStatusToDisk()
    }

    fun clearAll() {
        fundStatusOverrides.clear()
        myStatusOverrides.clear()
        participantOverrides.clear()
        prefs?.edit()?.clear()?.apply()
    }

    // ═══════════════════════════════════════════════════════
    // Persistence (только fundStatus — самое важное для cross-session)
    // ═══════════════════════════════════════════════════════

    private fun saveFundStatusToDisk() {
        val store = prefs ?: return
        try {
            // Сохраняем только Closed/Completed — Active нет смысла фиксировать,
            // оно дефолтное и серверу можно доверять.
            val toSave = fundStatusOverrides
                .filterValues { it != FundStatus.Active }
                .mapValues { it.value.name }
            val raw = json.encodeToString(
                MapSerializer(String.serializer(), String.serializer()),
                toSave
            )
            store.edit().putString(KEY_FUND_STATUS, raw).apply()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to save fund status overrides", e)
        }
    }

    private fun loadFundStatusFromDisk() {
        val store = prefs ?: return
        val raw = store.getString(KEY_FUND_STATUS, null) ?: return
        try {
            val map = json.decodeFromString(
                MapSerializer(String.serializer(), String.serializer()),
                raw
            )
            map.forEach { (id, statusName) ->
                runCatching { FundStatus.valueOf(statusName) }
                    .getOrNull()
                    ?.let { fundStatusOverrides[id] = it }
            }
            Log.d(TAG, "Loaded ${fundStatusOverrides.size} fund status overrides")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to load fund status overrides", e)
        }
    }
}
