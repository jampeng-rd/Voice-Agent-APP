package com.jam.voiceagent.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val AppColorScheme = lightColorScheme(
    background = Background,
    surface = Surface,
    surfaceContainerLow = SurfaceContainerLow,
    surfaceContainer = SurfaceContainer,
    surfaceContainerHigh = SurfaceContainerHigh,
    surfaceVariant = SurfaceVariant,
    primary = Primary,
    primaryContainer = PrimaryContainer,
    secondaryContainer = SecondaryContainer,
    onSurface = OnSurface,
    onSurfaceVariant = OnSurfaceVariant,
    outline = Outline,
    outlineVariant = OutlineVariant,
    error = Error
)

@Composable
fun VoiceAgentTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = Typography,
        content = content
    )
}
