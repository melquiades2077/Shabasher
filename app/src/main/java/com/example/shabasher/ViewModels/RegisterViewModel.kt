package com.example.shabasher.ViewModels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.shabasher.Mock.MockUsers

class RegisterViewModel: ViewModel() {
    var email = mutableStateOf("")
    var password = mutableStateOf("")
    var repeatPassword = mutableStateOf("")
    var error = mutableStateOf<String?>(null)

    fun validate(): Boolean {
        if (email.value.isBlank() || password.value.isBlank() || repeatPassword.value.isBlank()) {
            error.value = "Все поля должны быть заполнены"
            return false
        }

        if (password.value != repeatPassword.value) {
            error.value = "Пароли не совпадают"
            return false
        }

        if (MockUsers.any { it.email == email.value }) {
            error.value = "Пользователь уже существует"
            return false
        }

        error.value = null
        return true
    }
}