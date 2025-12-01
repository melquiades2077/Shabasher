package com.example.shabasher.ViewModels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class ShareEventViewModel: ViewModel() {
    var link = mutableStateOf("shabasher/app/join/123")
}