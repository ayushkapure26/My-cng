package com.example.ui.theme

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
    primary = NeonLime,
    onPrimary = DeepForest,
    primaryContainer = NeonDarkSurfaceVariant,
    onPrimaryContainer = NeonLime,
    secondary = NeonLime,
    onSecondary = DeepForest,
    tertiary = StatusAmberDark,
    background = NeonDarkBg,
    onBackground = NeonDarkText,
    surface = NeonDarkSurface,
    onSurface = NeonDarkText,
    surfaceVariant = NeonDarkSurfaceVariant,
    onSurfaceVariant = NeonDarkTextMuted,
    outline = NeonDarkBorder,
    outlineVariant = NeonDarkBorder.copy(alpha = 0.7f),
    error = StatusRedDark,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = NeonLime,
    onPrimary = DeepForest,
    primaryContainer = NeonLightSurfaceVariant,
    onPrimaryContainer = DeepForest,
    secondary = DeepForest,
    onSecondary = Color.White,
    tertiary = StatusAmberLight,
    background = NeonLightBg,
    onBackground = NeonLightText,
    surface = NeonLightSurface,
    onSurface = NeonLightText,
    surfaceVariant = NeonLightSurfaceVariant,
    onSurfaceVariant = NeonLightTextMuted,
    outline = NeonLightBorder,
    outlineVariant = NeonLightBorder.copy(alpha = 0.8f),
    error = StatusRedLight,
    onError = Color.White
)

@Composable
fun CNGTrackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Neon Street brand system prioritized for consistent mobile mobility aesthetic
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

