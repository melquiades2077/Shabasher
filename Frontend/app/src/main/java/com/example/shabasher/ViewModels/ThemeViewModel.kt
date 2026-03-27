package com.example.shabasher.ViewModels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.shabasher.Model.ThemeStorage
import kotlinx.coroutines.launch

class ThemeViewModel(app: Application) : AndroidViewModel(app) {

    private val context = app.applicationContext

    private val _isDarkTheme = mutableStateOf(true)
    val isDarkTheme: State<Boolean> = _isDarkTheme

    init {
        viewModelScope.launch {
            ThemeStorage.getTheme(context).collect { saved ->
                _isDarkTheme.value = saved
            }
        }
    }

    fun toggleTheme() {
        val newValue = !_isDarkTheme.value
        _isDarkTheme.value = newValue

        viewModelScope.launch {
            ThemeStorage.saveTheme(context, newValue)
        }
    }
}

