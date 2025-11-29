package com.example.shabasher.ViewModels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class NameViewModel: ViewModel() {
    var name = mutableStateOf("")

    var error = mutableStateOf<String?>(null)

    fun validate(): Boolean {
        if (name.value.isBlank()) {
            error.value = "Введите имя"
            return false
        }


        error.value = null
        return true
    }
}