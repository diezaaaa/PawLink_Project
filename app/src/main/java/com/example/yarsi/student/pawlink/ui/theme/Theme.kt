package com.example.yarsi.student.pawlink.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val PawLightColorScheme = lightColorScheme(
    primary          = PawPrimary,
    onPrimary        = PawWhite,
    primaryContainer = PawPrimaryLight,
    onPrimaryContainer = PawPrimaryDark,
    secondary        = PawAmber,
    onSecondary      = PawWhite,
    tertiary         = PawBlue,
    background       = PawSurface,
    onBackground     = PawOnSurface,
    surface          = PawSurface,
    onSurface        = PawOnSurface,
    outline          = PawBorder
)

private val PawDarkColorScheme = darkColorScheme(
    primary          = PawPrimaryDarkTheme,
    onPrimary        = PawPrimaryDark,
    primaryContainer = PawPrimaryDark,
    onPrimaryContainer = PawPrimaryLight,
    secondary        = PawAmber,
    onSecondary      = PawWhite,
    tertiary         = PawBlue,
    background       = PawSurfaceDark,
    onBackground     = PawOnSurfaceDark,
    surface          = PawSurfaceDark,
    onSurface        = PawOnSurfaceDark,
    outline          = PawGray
)

@Composable
fun PawLinkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // ← dimatikan agar warna PawLink selalu konsisten
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> PawDarkColorScheme
        else -> PawLightColorScheme
    }

    // Status bar ikut warna header PawLink
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = PawPrimaryDark.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}