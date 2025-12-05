package com.example.shabasher.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.firstOrNull

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

class TokenManager(private val context: Context) {
    companion object {
        private const val PREF_NAME = "token_preferences"
        private const val TOKEN_KEY = "jwt_token"
        private const val USER_ID_KEY = "user_id"
    }

    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    // Сохранить токен (синхронно)
    fun saveToken(token: String) {
        sharedPreferences.edit {
            putString(TOKEN_KEY, token)
        }
    }

    // Получить токен (синхронно)
    fun getToken(): String? {
        return sharedPreferences.getString(TOKEN_KEY, null)
    }

    // Получить токен как Flow (если нужно реактивное обновление)
    fun getTokenFlow(): Flow<String?> {
        return callbackFlow {
            // Начальное значение
            trySend(getToken())

            // Слушатель изменений в SharedPreferences
            val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                if (key == TOKEN_KEY) {
                    trySend(getToken())
                }
            }

            sharedPreferences.registerOnSharedPreferenceChangeListener(listener)

            // Очистка при отмене корутины
            awaitClose {
                sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
            }
        }
    }

    // Очистить токен (синхронно)
    fun clearToken() {
        sharedPreferences.edit {
            remove(TOKEN_KEY)
        }
    }

    // Проверить наличие токена (синхронно)
    fun hasToken(): Boolean {
        return getToken() != null
    }

    // Сохранить ID пользователя (синхронно)
    fun saveUserId(userId: String) {
        sharedPreferences.edit {
            putString(USER_ID_KEY, userId)
        }
    }

    // Получить ID пользователя (синхронно)
    fun getUserId(): String? {
        return sharedPreferences.getString(USER_ID_KEY, null)
    }

    // Очистить все данные (синхронно)
    fun clearAll() {
        sharedPreferences.edit {
            clear()
        }
    }
}