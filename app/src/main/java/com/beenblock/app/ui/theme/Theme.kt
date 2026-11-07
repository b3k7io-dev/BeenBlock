package com.beenblock.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkScheme = darkColorScheme(
    primary = White,
    onPrimary = Black,
    secondary = Porcelain,
    onSecondary = Black,
    tertiary = Silver,
    background = Graphite,
    onBackground = TextOnDark,
    surface = GraphiteSoft,
    onSurface = TextOnDark,
    surfaceVariant = Smoke,
    onSurfaceVariant = TextMutedDark,
    outline = Color(0x2EFFFFFF),
    error = Color(0xFFF0F0F2),
    onError = Black,
)

private val LightScheme = lightColorScheme(
    primary = Black,
    onPrimary = White,
    secondary = Graphite,
    onSecondary = White,
    tertiary = Smoke,
    background = Silver,
    onBackground = TextOnLight,
    surface = White,
    onSurface = TextOnLight,
    surfaceVariant = Porcelain,
    onSurfaceVariant = TextMutedLight,
    outline = Color(0x1F000000),
    error = Graphite,
    onError = White,
)

@Composable
fun BeenBlockTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val scheme = if (darkTheme) DarkScheme else LightScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }
    MaterialTheme(
        colorScheme = scheme,
        typography = AppTypography,
        content = content,
    )
}
