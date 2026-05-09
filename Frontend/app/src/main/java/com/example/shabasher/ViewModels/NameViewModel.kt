package com.example.shabasher.ViewModels

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shabasher.data.local.TokenManager
import com.example.shabasher.data.network.AuthRepository
import com.example.shabasher.data.network.NameRepository
import com.example.shabasher.data.network.ProfileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class NameViewModel(
    private val context: Context,
    private val email: String,
    private val password: String
) : ViewModel() {

    companion object {
        private const val MAX_NAME_LEN = 30
        private const val MAX_ABOUT_LEN = 150      // ← лимит "Обо мне"
        private const val MAX_TELEGRAM_LEN = 32    // ← лимит Telegram (username без @)
    }

    var name = mutableStateOf("")
    var aboutMe = mutableStateOf("")               // ← новое поле
    var telegram = mutableStateOf("")              // ← новое поле
    var avatarUri = mutableStateOf<Uri?>(null)     // выбранное фото (грузим после логина)

    var error = mutableStateOf<String?>(null)
    var loading = mutableStateOf(false)
    var success = mutableStateOf(false)

    private val authRepo = AuthRepository(context)
    private val nameRepo = NameRepository(context)

    fun submit() {
        success.value = false
        val trimmedName = name.value.trim()
        val trimmedAbout = aboutMe.value.trim()
        val trimmedTelegram = telegram.value.trim().removePrefix("@") // убираем @, если есть

        // --- Проверка имени ---
        if (trimmedName.isBlank()) {
            error.value = "Введите имя"
            return
        }
        if (trimmedName.length > MAX_NAME_LEN) {
            error.value = "Длина имени не должна превышать $MAX_NAME_LEN символов"
            return
        }

        // --- Опциональные проверки (только длина, если заполнено) ---
        if (trimmedAbout.length > MAX_ABOUT_LEN) {
            error.value = "Поле «Обо мне» слишком длинное (макс. $MAX_ABOUT_LEN символов)"
            return
        }
        if (trimmedTelegram.isNotEmpty() && trimmedTelegram.length > MAX_TELEGRAM_LEN) {
            error.value = "Имя пользователя Telegram слишком длинное (макс. $MAX_TELEGRAM_LEN символов)"
            return
        }

        loading.value = true
        error.value = null

        viewModelScope.launch {
            // --- Регистрация с новыми полями ---
            val register = authRepo.register(trimmedName, email, password, trimmedAbout, trimmedTelegram)
            if (register.isFailure) {
                error.value = register.exceptionOrNull()?.message ?: "Ошибка регистрации"
                loading.value = false
                return@launch
            }

            // --- Логин и получение токена ---
            val login = authRepo.login(email, password)
            if (login.isFailure) {
                error.value = login.exceptionOrNull()?.message ?: "Ошибка входа"
                loading.value = false
                return@launch
            }

            val token = authRepo.getToken()
            if (token == null) {
                error.value = "Ошибка: токен не сохранён"
                loading.value = false
                return@launch
            }

            val userId = decodeUserId(token)
            if (userId == null) {
                error.value = "Ошибка получения userId"
                loading.value = false
                return@launch
            }

            // --- Загрузка аватара (если выбран). Ошибка не блокирует регистрацию ---
            val pickedUri = avatarUri.value
            if (pickedUri != null) {
                runCatching {
                    val payload = withContext(Dispatchers.IO) {
                        val resolver: ContentResolver = context.contentResolver
                        val type = resolver.getType(pickedUri) ?: "image/jpeg"
                        val bytes = resolver.openInputStream(pickedUri)?.use { it.readBytes() }
                            ?: error("Не удалось прочитать файл")
                        Triple(bytes, fileNameFromUri(pickedUri, type), type)
                    }
                    val profileRepo = ProfileRepository(TokenManager(context))
                    profileRepo.uploadAvatar(payload.first, payload.second, payload.third)
                }
            }

            success.value = true
            loading.value = false
        }
    }
}

fun decodeUserId(jwt: String): String? {
    return try {
        val parts = jwt.split(".")
        val payload = String(android.util.Base64.decode(parts[1], android.util.Base64.DEFAULT))
        val json = JSONObject(payload)
        json.getString("userId")
    } catch (e: Exception) {
        null
    }
}

private fun fileNameFromUri(uri: Uri, mimeType: String): String {
    val ext = when (mimeType) {
        "image/png" -> "png"
        "image/webp" -> "webp"
        else -> "jpg"
    }
    val raw = uri.lastPathSegment?.substringAfterLast('/') ?: "avatar"
    val cleaned = raw.replace("[^A-Za-z0-9_.-]".toRegex(), "_").take(40)
    return if (cleaned.contains('.')) cleaned else "$cleaned.$ext"
}
