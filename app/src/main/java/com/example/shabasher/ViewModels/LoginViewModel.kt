package com.example.shabasher.ViewModels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.shabasher.Mock.MockUsers

class LoginViewModel: ViewModel() {
    var email = mutableStateOf("")
    var password = mutableStateOf("")
    var error = mutableStateOf<String?>(null)

    fun validate(): Boolean {
        if (email.value.isBlank() || password.value.isBlank()) {
            error.value = "Все поля должны быть заполнены"
            return false
        }


        if (!MockUsers.any { it.email == email.value }) {
            error.value = "Пользователь не существует"
            return false
        }

        if (!MockUsers.any { it.password == password.value }) {
            error.value = "Неверный пароль"
            return false
        }

        error.value = null
        return true
    }
}