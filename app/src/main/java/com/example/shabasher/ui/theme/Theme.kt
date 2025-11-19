package com.example.shabasher.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = DarkAccent,           // тот же акцент, можно оставить светлым
    onPrimary = Color.White,

    secondary = DarkButtons,        // кнопки вторичного уровня
    onSecondary = Color.White,

    background = DarkBackground,    // общий фон
    onBackground = Color.White,

    surface = DarkSurface,          // карточки, панели
    onSurface = Color.White,

    surfaceVariant = DarkInputs,    // инпуты
    onSurfaceVariant = Color.White,

    tertiary = LightTertiary


)

private val LightColorScheme = lightColorScheme(
    primary = LightAccent,          // главный акцент
    onPrimary = Color.White,        // текст на акцентных кнопках

    secondary = LightButtons,       // вторичные кнопки
    onSecondary = Color.Black,

    background = LightBackground,   // главный фон
    onBackground = Color.Black,

    surface = LightSurface,         // карточки, белые блоки
    onSurface = Color.Black,

    surfaceVariant = LightInputs,   // инпуты, дополнительные поверхности
    onSurfaceVariant = Color.Black,

    tertiary = DarkTertiary

)

@Composable
fun ShabasherTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}