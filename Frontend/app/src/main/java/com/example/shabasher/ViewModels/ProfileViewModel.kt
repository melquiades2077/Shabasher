// ViewModels/ProfileViewModel.kt
package com.example.shabasher.ViewModels

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shabasher.data.network.ProfileRepository
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val context: Context
) : ViewModel() {
    private val repo = ProfileRepository(context)

    var userName = mutableStateOf("Загрузка...")
    var userEmail = mutableStateOf("")
    var loading = mutableStateOf(false)
    var error = mutableStateOf<String?>(null)

    fun loadProfile() {
        loading.value = true
        error.value = null

        viewModelScope.launch {
            repo.getProfile()
                .onSuccess { profile ->
                    userName.value = profile.name
                    userEmail.value = profile.email
                }
                .onFailure {
                    error.value = it.message ?: "Ошибка загрузки профиля"
                    userName.value = "Ошибка"
                }

            loading.value = false
        }
    }
}