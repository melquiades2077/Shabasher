package com.example.shabasher.ViewModels

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shabasher.data.network.AuthRepository // ← ИЗМЕНИТЬ ИМПОРТ
import kotlinx.coroutines.launch

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
        if (email.value.isBlank() || password.value.isBlank() || repeatPassword.value.isBlank()) {
            error.value = "Все поля должны быть заполнены"
            return
        }

        if (password.value != repeatPassword.value) {
            error.value = "Пароли не совпадают"
            return
        }

        success.value = true
    }

}