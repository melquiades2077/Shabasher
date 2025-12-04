package com.example.shabasher.ViewModels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class ShareEventViewModel(
    private val repo: EventInvitesRepository = EventInvitesRepository()
) : ViewModel() {

    var link = mutableStateOf("")

    fun init(eventId: String) {
        if (link.value.isNotEmpty()) return

        viewModelScope.launch {
            // В будущем это будет запрос к бекенду
            val shortLink = repo.getInviteLink(eventId)
            link.value = shortLink
        }
    }
}
