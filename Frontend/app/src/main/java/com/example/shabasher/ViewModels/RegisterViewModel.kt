package com.example.shabasher.ViewModels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shabasher.Mock.MockUsers
import com.example.shabasher.data.AuthRepository
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val repo: AuthRepository = AuthRepository()
) : ViewModel() {

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

        loading.value = true
        error.value = null

        viewModelScope.launch {
            repo.register(email.value, password.value)
                .onSuccess {
                    success.value = true
                }
                .onFailure {
                    error.value = it.message
                }

            loading.value = false
        }
    }
}