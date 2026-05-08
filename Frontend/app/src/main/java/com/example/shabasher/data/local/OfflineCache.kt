package com.example.shabasher.data.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.shabasher.Model.EventShort
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

/**
 * Простое off-line хранилище основных сущностей.
 *
 * Singleton. Persistence через SharedPreferences (JSON).
 * Цель — при отсутствии связи показывать последние известные данные,
 * а при наличии связи — мгновенно отрисовать кеш и параллельно обновить с сети.
 */
object OfflineCache {

    private const val TAG = "OfflineCache"
    private const val PREFS_NAME = "offline_cache"

    private const val KEY_EVENTS = "events_list"
    private const val KEY_FUNDRAISES_PREFIX = "fundraises_for_event_"
    private const val KEY_FUNDRAISE_DETAILS_PREFIX = "fundraise_details_"

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        if (prefs != null) return
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // ═══════════════════════════════════════════════════════
    // Список событий
    // ═══════════════════════════════════════════════════════

    fun saveEvents(events: List<EventShort>) {
        try {
            val cached = events.map { it.toCache() }
            val raw = json.encodeToString(ListSerializer(EventShortCache.serializer()), cached)
            prefs?.edit()?.putString(KEY_EVENTS, raw)?.apply()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to save events", e)
        }
    }

    fun loadEvents(): List<EventShort>? {
        val raw = prefs?.getString(KEY_EVENTS, null) ?: return null
        return try {
            json.decodeFromString(ListSerializer(EventShortCache.serializer()), raw)
                .map { it.toDomain() }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to load events", e)
            null
        }
    }

    // ═══════════════════════════════════════════════════════
    // Список сборов в событии (хранится JSON-строкой целиком)
    // ═══════════════════════════════════════════════════════

    fun saveFundraisesJson(eventId: String, json: String) {
        prefs?.edit()?.putString(KEY_FUNDRAISES_PREFIX + eventId, json)?.apply()
    }

    fun loadFundraisesJson(eventId: String): String? =
        prefs?.getString(KEY_FUNDRAISES_PREFIX + eventId, null)

    // ═══════════════════════════════════════════════════════
    // Детали сбора
    // ═══════════════════════════════════════════════════════

    fun saveFundraiseDetailsJson(fundraiseId: String, json: String) {
        prefs?.edit()?.putString(KEY_FUNDRAISE_DETAILS_PREFIX + fundraiseId, json)?.apply()
    }

    fun loadFundraiseDetailsJson(fundraiseId: String): String? =
        prefs?.getString(KEY_FUNDRAISE_DETAILS_PREFIX + fundraiseId, null)

    fun clearAll() {
        prefs?.edit()?.clear()?.apply()
    }

    // ═══════════════════════════════════════════════════════
    // Сериализуемая копия EventShort
    // ═══════════════════════════════════════════════════════

    @Serializable
    private data class EventShortCache(
        val id: String,
        val title: String,
        val date: String,
        val status: String,
        val avatarUrl: String? = null
    ) {
        fun toDomain() = EventShort(id = id, title = title, date = date, status = status, avatarUrl = avatarUrl)
    }

    private fun EventShort.toCache() = EventShortCache(
        id = id, title = title, date = date, status = status, avatarUrl = avatarUrl
    )
}
