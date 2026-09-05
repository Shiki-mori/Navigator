package dev.phrolova.navigator.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Teal = Color(0xFF0F6B5C)
private val TealDark = Color(0xFF7AD1C2)

private val LightColors = lightColorScheme(
    primary = Teal,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD4EFE9),
    secondary = Color(0xFF3F5B76),
    error = Color(0xFFB42318),
    errorContainer = Color(0xFFF9D2CE),
    background = Color(0xFFF7F8F6),
    surface = Color(0xFFF7F8F6),
)

private val DarkColors = darkColorScheme(
    primary = TealDark,
    onPrimary = Color(0xFF00382F),
    primaryContainer = Color(0xFF0B4F44),
    secondary = Color(0xFFB4C7DB),
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF8C1D18),
    background = Color(0xFF121412),
    surface = Color(0xFF121412),
)

@Composable
fun NavigatorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
