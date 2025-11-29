package com.example.shabasher.Model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object SafeNavigation {
    public var isNavigating = false

    fun navigate(action: () -> Unit) {
        if (!isNavigating) {
            isNavigating = true
            action()

            CoroutineScope(Dispatchers.Main).launch {
                delay(1000)
                isNavigating = false
            }
        }
    }
}