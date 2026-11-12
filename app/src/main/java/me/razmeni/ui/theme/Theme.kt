package me.razmeni.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = Green40,
    onPrimary = Color.White,
    primaryContainer = Green90,
    onPrimaryContainer = Green10,
    secondary = Sage40,
    onSecondary = Color.White,
    secondaryContainer = Sage90,
    onSecondaryContainer = Sage10,
    background = LightSurface,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = LightContainerLow,
    surfaceContainer = LightContainer,
    surfaceContainerHigh = LightContainerHigh,
    surfaceContainerHighest = LightContainerHighest
)

private val DarkColors = darkColorScheme(
    primary = Green80,
    onPrimary = Green20,
    primaryContainer = Green30,
    onPrimaryContainer = Green90,
    secondary = Sage80,
    onSecondary = Sage20,
    secondaryContainer = Sage30,
    onSecondaryContainer = Sage90,
    background = DarkSurface,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkSurfaceVariant,
    surfaceContainerLowest = DarkContainerLowest,
    surfaceContainerLow = DarkContainerLow,
    surfaceContainer = DarkContainer,
    surfaceContainerHigh = DarkContainerHigh,
    surfaceContainerHighest = DarkContainerHighest
)

private val AppShapes = Shapes(medium = RoundedCornerShape(16.dp))

@Composable
fun RazmeniTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
