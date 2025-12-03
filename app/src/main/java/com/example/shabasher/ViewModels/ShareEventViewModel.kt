package com.example.shabasher.ViewModels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class ShareEventViewModel : ViewModel() {

    // ссылка, которую отдаёт backend
    var link = mutableStateOf("")

    // eventId должен приходить извне, через SavedStateHandle
    fun init(eventId: String) {
        if (link.value.isNotEmpty()) return // чтобы не пересоздавать

        // В будущем здесь будет результат запроса backend
        // например backend отдаст short link
        link.value = "https://shabasher.app/join?eventId=$eventId"
    }
}
