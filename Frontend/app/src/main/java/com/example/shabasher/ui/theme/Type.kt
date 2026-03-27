package com.example.shabasher.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.shabasher.R

public val ShabasherFont = FontFamily(
    Font(R.font.shabasher_font)
)

val Typography = Typography(

    // === КРУПНЫЕ ЗАГОЛОВКИ ===
    // Лучше для приветственных экранов, больших секций
    displayMedium = TextStyle(
        fontFamily = ShabasherFont,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 38.sp,
        lineHeight = 38.sp
    ),

    // Главный заголовок экрана
    headlineLarge = TextStyle(
        fontFamily = ShabasherFont,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp
    ),

    // Заголовки секций
    headlineMedium = TextStyle(
        fontFamily = ShabasherFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),

    // === ТЕКСТ ДЛЯ UI ===
    titleLarge = TextStyle(
        fontFamily = ShabasherFont,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),

    titleMedium = TextStyle(
        fontFamily = ShabasherFont,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),

    // === ОСНОВНОЙ ТЕКСТ ===
    bodyLarge = TextStyle(
        fontFamily = ShabasherFont,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),

    // Чаще всего используется
    bodyMedium = TextStyle(
        fontFamily = ShabasherFont,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),

    // Мелкие пояснения
    bodySmall = TextStyle(
        fontFamily = ShabasherFont,
        fontWeight = FontWeight.Light,
        fontSize = 12.sp,
        lineHeight = 18.sp
    ),

    // === ТЕКСТ ДЛЯ КНОПОК ===
    labelLarge = TextStyle(
        fontFamily = ShabasherFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 20.sp
    ),

    labelMedium = TextStyle(
        fontFamily = ShabasherFont,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 16.sp
    ),

    // Подписи под InputField
    labelSmall = TextStyle(
        fontFamily = ShabasherFont,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )
)
