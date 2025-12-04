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

    var name = mutableStateOf("")
    var error = mutableStateOf<String?>(null)
    var loading = mutableStateOf(false)
    var success = mutableStateOf(false)

    private val authRepo = AuthRepository(context)
    private val nameRepo = NameRepository(context)

    fun submit() {
        if (name.value.isBlank()) {
            error.value = "Введите имя"
            return
        }

        loading.value = true
        error.value = null

        viewModelScope.launch {

            // --- 1) REGISTER FIRST ---
            val register = authRepo.register(email, password)
            if (register.isFailure) {
                error.value = register.exceptionOrNull()?.message ?: "Ошибка регистрации"
                loading.value = false
                return@launch
            }

            // --- 2) LOGIN ---
            val login = authRepo.login(email, password)
            if (login.isFailure) {
                error.value = login.exceptionOrNull()?.message ?: "Ошибка входа"
                loading.value = false
                return@launch
            }

            // --- 3) GET TOKEN ---
            val token = authRepo.getToken()
            if (token == null) {
                error.value = "Ошибка: токен не сохранён"
                loading.value = false
                return@launch
            }

            // --- 4) PARSE USER ID FROM JWT ---
            val userId = decodeUserId(token)
            if (userId == null) {
                error.value = "Ошибка получения userId"
                loading.value = false
                return@launch
            }

            // --- 5) SET NAME ---
            val result = nameRepo.setUserName(userId, name.value, token)

            if (result.isSuccess) {
                success.value = true
            } else {
                error.value = result.exceptionOrNull()?.message ?: "Ошибка сохранения имени"
            }

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

