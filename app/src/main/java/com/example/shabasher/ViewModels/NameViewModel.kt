package com.example.shabasher.ViewModels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shabasher.data.NameRepository
import com.example.shabasher.data.ProfileRepository
import kotlinx.coroutines.launch

class NameViewModel(
    private val repo: NameRepository = NameRepository()
) : ViewModel() {

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
            repo.setUserName(name.value)
                .onSuccess {
                    success.value = true
                }
                .onFailure {
                    error.value = it.message ?: "Ошибка"
                }

            loading.value = false
        }
    }
}
