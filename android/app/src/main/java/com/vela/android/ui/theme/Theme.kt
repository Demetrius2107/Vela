package com.vela.android.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val VelaColorScheme = lightColorScheme(
    primary = VelaPrimary,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = VelaPrimaryLight,
    secondary = VelaSuccess,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    error = VelaError,
    onError = androidx.compose.ui.graphics.Color.White,
    surface = androidx.compose.ui.graphics.Color.White,
    onSurface = VelaTextPrimary,
    surfaceVariant = VelaBgGray,
    outline = VelaTextHint,
    background = VelaBgLight,
    onBackground = VelaTextPrimary,
)

@Composable
fun VelaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = VelaColorScheme,
        content = content
    )
}
