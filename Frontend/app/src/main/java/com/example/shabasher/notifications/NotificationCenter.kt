package com.example.shabasher.notifications

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.shabasher.Model.AppNotification
import com.example.shabasher.Model.NotificationCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import java.time.Instant
import java.util.UUID

/**
 * Центр in-app уведомлений.
 *
 * Singleton. Хранит список уведомлений в SharedPreferences (JSON).
 * Любое место в коде (ViewModel, Repository, Worker) может вызвать `notify(...)`,
 * и оно автоматически:
 *   1. Появится в in-app центре уведомлений (Bell-иконка с badge → экран списка)
 *   2. Появится в системной шторке Android (если категория не System)
 *
 * Хранилище живёт между перезапусками приложения. Лимит — 100 последних уведомлений
 * (старые удаляются автоматически).
 */
object NotificationCenter {

    private const val TAG = "NotificationCenter"
    private const val PREFS_NAME = "notifications_store"
    private const val KEY_LIST = "items_json"
    private const val MAX_NOTIFICATIONS = 100

    private val json = Json { ignoreUnknownKeys = true }

    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    private var prefs: SharedPreferences? = null
    private var systemNotifier: SystemNotifier? = null

    /** Должно вызываться один раз при старте Application/MainActivity. */
    fun init(context: Context) {
        if (prefs != null) return
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        systemNotifier = SystemNotifier(context.applicationContext)
        systemNotifier?.ensureChannelsCreated()
        _notifications.value = loadFromDisk()
    }

    /**
     * Создать новое уведомление.
     *
     * @param dedupKey если задан и за последние 60 секунд уже было уведомление с таким же
     *                 sourceKey — повторное создание игнорируется (защита от спама).
     * @param showSystemNotification по умолчанию true — Android покажет в шторке.
     */
    fun notify(
        category: NotificationCategory,
        title: String,
        body: String,
        targetRoute: String? = null,
        sourceKey: String? = null,
        dedupKey: String? = sourceKey,
        showSystemNotification: Boolean = true
    ) {
        val now = Instant.now()

        // Дедупликация: если только что (60 сек) уже было уведомление с тем же ключом — скип.
        if (dedupKey != null) {
            val recent = _notifications.value.firstOrNull { it.sourceKey == dedupKey }
            if (recent != null) {
                val recentInstant = runCatching { Instant.parse(recent.createdAtIso) }
                    .getOrDefault(Instant.EPOCH)
                if (now.toEpochMilli() - recentInstant.toEpochMilli() < 60_000) {
                    Log.d(TAG, "Deduped: $title (key=$dedupKey)")
                    return
                }
            }
        }

        val notification = AppNotification(
            id = UUID.randomUUID().toString(),
            category = category,
            title = title,
            body = body,
            targetRoute = targetRoute,
            createdAtIso = now.toString(),
            isRead = false,
            sourceKey = sourceKey
        )

        val updated = (listOf(notification) + _notifications.value).take(MAX_NOTIFICATIONS)
        _notifications.value = updated
        saveToDisk(updated)

        if (showSystemNotification) {
            systemNotifier?.show(notification)
        }
    }

    fun markAsRead(id: String) {
        val updated = _notifications.value.map { if (it.id == id) it.copy(isRead = true) else it }
        _notifications.value = updated
        saveToDisk(updated)
    }

    fun markAllAsRead() {
        val updated = _notifications.value.map { it.copy(isRead = true) }
        _notifications.value = updated
        saveToDisk(updated)
    }

    fun delete(id: String) {
        val updated = _notifications.value.filterNot { it.id == id }
        _notifications.value = updated
        saveToDisk(updated)
        systemNotifier?.cancel(id)
    }

    fun clearAll() {
        _notifications.value = emptyList()
        saveToDisk(emptyList())
        systemNotifier?.cancelAll()
    }

    fun unreadCountFlow(): kotlinx.coroutines.flow.Flow<Int> =
        notifications.map { list -> list.count { !it.isRead } }

    // ═══════════════════════════════════════════════════════
    // Persistence
    // ═══════════════════════════════════════════════════════

    private fun loadFromDisk(): List<AppNotification> {
        val raw = prefs?.getString(KEY_LIST, null) ?: return emptyList()
        return try {
            json.decodeFromString<List<AppNotification>>(raw)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to decode notifications, resetting", e)
            emptyList()
        }
    }

    private fun saveToDisk(list: List<AppNotification>) {
        try {
            val raw = json.encodeToString(
                kotlinx.serialization.builtins.ListSerializer(AppNotification.serializer()),
                list
            )
            prefs?.edit()?.putString(KEY_LIST, raw)?.apply()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save notifications", e)
        }
    }
}
