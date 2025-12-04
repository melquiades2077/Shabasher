package com.example.shabasher.ViewModels

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shabasher.data.network.AuthRepository // ← ИЗМЕНИТЬ ИМПОРТ
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class RegisterViewModel(
    private val context: Context
) : ViewModel() {

    private val repo = AuthRepository(context)

    var email = mutableStateOf("")
    var password = mutableStateOf("")
    var repeatPassword = mutableStateOf("")
    var error = mutableStateOf<String?>(null)
    var loading = mutableStateOf(false)
    var success = mutableStateOf(false)

    fun register() {

        // 1. Проверка email
        EmailValidator.validate(email.value)?.let {
            error.value = it
            return
        }

        // 2. Проверка пароля
        PasswordValidator.validate(password.value)?.let {
            error.value = it
            return
        }

        // 3. Пароли совпадают?
        if (password.value != repeatPassword.value) {
            error.value = "Пароли не совпадают"
            return
        }

        // 4. Успех (или отправка на сервер)
        success.value = true
    }
}

object EmailValidator {

    private const val MAX_EMAIL_LEN = 254

    private val regex = Pattern.compile(
        "(?<Login>[a-zA-Z0-9._%+-]+)@(?<Domain>[a-zA-Z0-9.-]+)\\.(?<HLDomain>[a-zA-Z]{2,})\\b",
        Pattern.CASE_INSENSITIVE
    )

    fun validate(email: String): String? {
        val errors = mutableListOf<String>()

        if (email.isBlank())
            errors.add("Электронная почта обязательна для заполнения")

        if (email.length > MAX_EMAIL_LEN)
            errors.add("Длина электронной почты не должна превышать $MAX_EMAIL_LEN")

        try {
            if (!regex.matcher(email).find())
                errors.add("Неверный формат электронной почты")
        } catch (e: Exception) {
            errors.add("Неверный формат электронной почты")
        }

        return if (errors.isEmpty()) null else errors.joinToString("; ")
    }
}

object PasswordValidator {

    private const val MAX_PASSWORD_LEN = 64
    private const val MIN_PASSWORD_LEN = 8

    fun validate(password: String): String? {
        val errors = mutableListOf<String>()

        if (password.isBlank() || password.length < MIN_PASSWORD_LEN)
            errors.add("Пароль должен содержать минимум $MIN_PASSWORD_LEN символов")

        if (password.length > MAX_PASSWORD_LEN)
            errors.add("Длина пароля не должна превышать $MAX_PASSWORD_LEN")

        return if (errors.isEmpty()) null else errors.joinToString("; ")
    }
}
