package com.example.shabasher.ViewModels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class CreateEventViewModel: ViewModel() {
    var title = mutableStateOf("")
    var description = mutableStateOf("")
    var event_address = mutableStateOf("")
    var date = mutableStateOf("")
    var time = mutableStateOf("")


    var error = mutableStateOf<String?>(null)




    fun validate(): Boolean {
        if (title.value.isBlank()) {
            error.value = "Введите имя"
            return false
        }

        if (title.value.isBlank() || description.value.isBlank() || event_address.value.isBlank() || date.value.isBlank() || time.value.isBlank()) {
            error.value = "Все поля должны быть заполнены"
            return false
        }


        error.value = null
        return true
    }
}

