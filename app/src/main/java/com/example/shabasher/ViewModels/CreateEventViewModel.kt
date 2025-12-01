package com.example.shabasher.ViewModels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class CreateEventViewModel : ViewModel() {

    var title = mutableStateOf("")
    var description = mutableStateOf("")
    var address = mutableStateOf("")
    var date = mutableStateOf("")
    var time = mutableStateOf("")

    var error = mutableStateOf<String?>(null)

    fun setDate(d: String) {
        date.value = d
    }

    fun setTime(h: Int, m: Int) {
        time.value = "%02d:%02d".format(h, m)
    }

    fun validate(): Boolean {
        if (title.value.isBlank() ||
            description.value.isBlank() ||
            address.value.isBlank() ||
            date.value.isBlank() ||
            time.value.isBlank()
        ) {
            error.value = "Заполните все поля"
            return false
        }

        error.value = null
        return true
    }
}


