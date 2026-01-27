package com.example.shabasher.ViewModels

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shabasher.data.network.AuthRepository
import com.example.shabasher.data.network.NameRepository
import kotlinx.coroutines.launch
import org.json.JSONObject

class NameViewModel(
    private val context: Context,
    private val email: String,
    private val password: String
) : ViewModel() {

    companion object {
        private const val MAX_NAME_LEN = 30   // ← лимит длины имени
    }

    var name = mutableStateOf("")
    var error = mutableStateOf<String?>(null)
    var loading = mutableStateOf(false)
    var success = mutableStateOf(false)

    private val authRepo = AuthRepository(context)
    private val nameRepo = NameRepository(context)

    fun submit() {
        success.value = false
        val value = name.value.trim()

        // --- 1) Проверка имени ---
        if (value.isBlank()) {
            error.value = "Введите имя"
            return
        }

        if (value.length > MAX_NAME_LEN) {
            error.value = "Длина имени не должна превышать $MAX_NAME_LEN символов"
            return
        }

        loading.value = true
        error.value = null

        viewModelScope.launch {

            // --- 2) REGISTER FIRST ---
            val register = authRepo.register(value, email, password)
            if (register.isFailure) {
                error.value = register.exceptionOrNull()?.message ?: "Ошибка регистрации"
                loading.value = false
                return@launch
            }

            // --- 3) LOGIN ---
            val login = authRepo.login(email, password)
            if (login.isFailure) {
                error.value = login.exceptionOrNull()?.message ?: "Ошибка входа"
                loading.value = false
                return@launch
            }

            // --- 4) GET TOKEN ---
            val token = authRepo.getToken()
            if (token == null) {
                error.value = "Ошибка: токен не сохранён"
                loading.value = false
                return@launch
            }

            // --- 5) PARSE USER ID ---
            val userId = decodeUserId(token)
            if (userId == null) {
                error.value = "Ошибка получения userId"
                loading.value = false
                return@launch
            }

            success.value = true

            /*
            ЭТО ВАЩЕ НЕ ЗНАЮ ЧО. НЕЙРОШЛАК
            // --- 6) SAVE NAME ---
            val result = nameRepo.setUserName(userId, value, token)

            if (result.isSuccess) {
                success.value = true
            } else {
                error.value = result.exceptionOrNull()?.message ?: "Ошибка сохранения имени"
            }

            loading.value = false
                         */
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
