package com.example.shabasher.ViewModels

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shabasher.data.network.AuthRepository
import com.example.shabasher.data.network.NameRepository
import kotlinx.coroutines.launch

class NameViewModel(
    private val context: Context
) : ViewModel() {
    private val nameRepo = NameRepository()
    private val authRepo = AuthRepository(context)

    var name = mutableStateOf("")
    var error = mutableStateOf<String?>(null)
    var loading = mutableStateOf(false)
    var success = mutableStateOf(false)

    fun submitName() {
        if (name.value.isBlank()) {
            error.value = "Введите имя"
            return
        }

        loading.value = true
        error.value = null

        viewModelScope.launch {
            val token = authRepo.getToken()

            if (token == null) {
                error.value = "Ошибка авторизации"
                loading.value = false
                return@launch
            }

            // ★ 2. Отправляем имя с токеном
            nameRepo.setUserName(name.value, token)
                .onSuccess { response ->
                    success.value = true
                    println("Имя успешно сохранено: ${response.name}")
                }
                .onFailure { exception ->
                    error.value = exception.message ?: "Ошибка сохранения имени"
                }

            loading.value = false
        }
    }
}