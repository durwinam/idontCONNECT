package ir.idontconnect.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import ir.idontconnect.data.settings.ThemeMode

private val NeonCyan = Color(0xFF53E6D0)
private val NeonPurple = Color(0xFFB69CFF)
private val DarkScheme = darkColorScheme(
    primary = NeonCyan,
    secondary = NeonPurple,
    background = Color(0xFF090C13),
    surface = Color(0xFF121824),
    onPrimary = Color(0xFF06110F),
    onBackground = Color(0xFFF2F5FA),
    onSurface = Color(0xFFF2F5FA),
)
private val LightScheme = lightColorScheme(
    primary = Color(0xFF087F77),
    secondary = Color(0xFF7151C8),
    background = Color(0xFFF4F7FB),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onBackground = Color(0xFF18202D),
    onSurface = Color(0xFF18202D),
)

@Composable
fun IdontConnectTheme(
    mode: ThemeMode = ThemeMode.SYSTEM,
    accentIndex: Int = 0,
    content: @Composable () -> Unit,
) {
    val accent = when (accentIndex.coerceIn(0, 3)) {
        1 -> Color(0xFFB69CFF)
        2 -> Color(0xFF6AA8FF)
        3 -> Color(0xFFFF9DCA)
        else -> NeonCyan
    }
    val lightAccent = when (accentIndex.coerceIn(0, 3)) {
        1 -> Color(0xFF7151C8)
        2 -> Color(0xFF315FBE)
        3 -> Color(0xFFB64B7E)
        else -> Color(0xFF087F77)
    }
    val dark = when (mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val scheme = if (dark) DarkScheme.copy(primary = accent, secondary = if (accentIndex == 1) NeonCyan else NeonPurple)
    else LightScheme.copy(
        primary = lightAccent,
        secondary = if (accentIndex == 1) Color(0xFF087F77) else Color(0xFF7151C8),
    )
    MaterialTheme(colorScheme = scheme, content = content)
}
