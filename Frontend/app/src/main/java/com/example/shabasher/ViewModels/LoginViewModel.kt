package com.example.shabasher.ViewModels

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shabasher.data.network.AuthRepository // ← ИЗМЕНИТЬ ИМПОРТ
import kotlinx.coroutines.launch

class LoginViewModel(
    private val context: Context // ← ДОБАВИТЬ Context
) : ViewModel() {
    private val repo = AuthRepository(context) // ← СОЗДАТЬ С Context

    var email = mutableStateOf("")
    var password = mutableStateOf("")
    var error = mutableStateOf<String?>(null)
    var loading = mutableStateOf(false)
    var success = mutableStateOf(false)

    fun login() {
        if (email.value.isBlank() || password.value.isBlank()) {
            error.value = "Все поля должны быть заполнены"
            return
        }

        loading.value = true
        error.value = null

        viewModelScope.launch {
            repo.login(email.value, password.value)
                .onSuccess {
                    success.value = true
                }
                .onFailure {
                    error.value = when (it.message) {
                        "\"Пользователь с данным email не найден\"" ->
                            "Такого email не существует"

                        "\"Неверный пароль\"" ->
                            "Неверный пароль"

                        else ->
                            it.message ?: "Ошибка входа"
                    }
                }

            loading.value = false
        }
    }
}