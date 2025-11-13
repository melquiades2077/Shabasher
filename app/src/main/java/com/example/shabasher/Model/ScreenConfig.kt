package com.example.shabasher.Model

data class ScreenConfig(
    val title: String? = null,
    val showTopBar: Boolean = false,
    val showBottomBar: Boolean = false,
    val showFab: Boolean = false,
    val fabAction: (() -> Unit)? = null
)

