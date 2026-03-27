package com.example.shabasher.Model

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.themeDataStore by preferencesDataStore("theme_settings")

object ThemeStorage {

    private val DARK_THEME_KEY = booleanPreferencesKey("dark_theme")

    fun getTheme(context: Context): Flow<Boolean> =
        context.themeDataStore.data.map { prefs ->
            prefs[DARK_THEME_KEY] ?: true   // по умолчанию тёмная
        }

    suspend fun saveTheme(context: Context, isDark: Boolean) {
        context.themeDataStore.edit { prefs ->
            prefs[DARK_THEME_KEY] = isDark
        }
    }
}
