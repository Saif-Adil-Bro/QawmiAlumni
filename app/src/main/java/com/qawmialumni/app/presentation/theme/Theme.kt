package com.qawmialumni.app.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = EmeraldOnPrimary,
    primaryContainer = EmeraldPrimaryContainer,
    onPrimaryContainer = EmeraldOnPrimaryContainer,
    secondary = HeritageGold,
    onSecondary = HeritageGoldOn,
    secondaryContainer = HeritageGoldContainer,
    onSecondaryContainer = HeritageGoldOnContainer,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
)

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldPrimaryContainer,
    onPrimary = EmeraldOnPrimaryContainer,
    primaryContainer = EmeraldPrimary,
    onPrimaryContainer = EmeraldPrimaryContainer,
    secondary = HeritageGold,
    onSecondary = HeritageGoldOn,
    secondaryContainer = HeritageGoldContainer,
    onSecondaryContainer = HeritageGoldOnContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
)

@Composable
fun QawmiAlumniTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
