package com.example.shabasher.ViewModels

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shabasher.data.network.InviteRepository
import kotlinx.coroutines.launch
import androidx.compose.runtime.State

class ShareEventViewModel(private val inviteRepository: InviteRepository) : ViewModel() {

    var link = mutableStateOf("")
    private val _loading = mutableStateOf(false)
    val loading: State<Boolean> = _loading

    // Инициализация данных для страницы
    fun init(eventId: String, context: Context) {
        if (link.value.isNotEmpty()) return // чтобы не пересоздавать

        _loading.value = true
        viewModelScope.launch {
            try {
                val result = inviteRepository.createInvite(eventId) // Запрос на создание ссылки приглашения
                if (result.isSuccess) {
                    link.value = result.getOrNull() ?: ""
                } else {
                    Toast.makeText(context, "Ошибка при получении ссылки", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Ошибка соединения", Toast.LENGTH_SHORT).show()
            } finally {
                _loading.value = false
            }
        }
    }
}




