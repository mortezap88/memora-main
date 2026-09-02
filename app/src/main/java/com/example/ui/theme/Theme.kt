package com.example.ui.theme

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = LightTertiary,
    onTertiary = LightOnTertiary,
    tertiaryContainer = LightTertiaryContainer,
    onTertiaryContainer = LightOnTertiaryContainer,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline
)

private val PinkColorScheme = lightColorScheme(
    primary = PinkPrimary,
    onPrimary = PinkOnPrimary,
    primaryContainer = PinkPrimaryContainer,
    onPrimaryContainer = PinkOnPrimaryContainer,
    secondary = PinkPrimary,
    onSecondary = Color.White,
    background = PinkBackground,
    onBackground = PinkOnSurface,
    surface = PinkSurface,
    onSurface = PinkOnSurface,
    surfaceVariant = PinkSurfaceVariant,
    onSurfaceVariant = PinkOnSurfaceVariant,
    outline = PinkOutline
)

private val YellowColorScheme = lightColorScheme(
    primary = YellowPrimary,
    onPrimary = YellowOnPrimary,
    primaryContainer = YellowPrimaryContainer,
    onPrimaryContainer = YellowOnPrimaryContainer,
    secondary = YellowPrimary,
    onSecondary = Color.White,
    background = YellowBackground,
    onBackground = YellowOnSurface,
    surface = YellowSurface,
    onSurface = YellowOnSurface,
    surfaceVariant = YellowSurfaceVariant,
    onSurfaceVariant = YellowOnSurfaceVariant,
    outline = YellowOutline
)

private val KittyColorScheme = lightColorScheme(
    primary = KittyPrimary,
    onPrimary = KittyOnPrimary,
    primaryContainer = KittyPrimaryContainer,
    onPrimaryContainer = KittyOnPrimaryContainer,
    secondary = KittyPrimary,
    onSecondary = Color.White,
    background = KittyBackground,
    onBackground = KittyOnSurface,
    surface = KittySurface,
    onSurface = KittyOnSurface,
    surfaceVariant = KittySurfaceVariant,
    onSurfaceVariant = KittyOnSurfaceVariant,
    outline = KittyOutline
)

private val MintColorScheme = lightColorScheme(
    primary = MintPrimary,
    onPrimary = MintOnPrimary,
    primaryContainer = MintPrimaryContainer,
    onPrimaryContainer = MintOnPrimaryContainer,
    secondary = MintPrimary,
    onSecondary = Color.White,
    background = MintBackground,
    onBackground = MintOnSurface,
    surface = MintSurface,
    onSurface = MintOnSurface,
    surfaceVariant = MintSurfaceVariant,
    onSurfaceVariant = MintOnSurfaceVariant,
    outline = MintOutline
)

private val OceanColorScheme = lightColorScheme(
    primary = OceanPrimary,
    onPrimary = OceanOnPrimary,
    primaryContainer = OceanPrimaryContainer,
    onPrimaryContainer = OceanOnPrimaryContainer,
    secondary = OceanPrimary,
    onSecondary = Color.White,
    background = OceanBackground,
    onBackground = OceanOnSurface,
    surface = OceanSurface,
    onSurface = OceanOnSurface,
    surfaceVariant = OceanSurfaceVariant,
    onSurfaceVariant = OceanOnSurfaceVariant,
    outline = OceanOutline
)

private val CosmicColorScheme = lightColorScheme(
    primary = CosmicPrimary,
    onPrimary = CosmicOnPrimary,
    primaryContainer = CosmicPrimaryContainer,
    onPrimaryContainer = CosmicOnPrimaryContainer,
    secondary = CosmicPrimary,
    onSecondary = Color.White,
    background = CosmicBackground,
    onBackground = CosmicOnSurface,
    surface = CosmicSurface,
    onSurface = CosmicOnSurface,
    surfaceVariant = CosmicSurfaceVariant,
    onSurfaceVariant = CosmicOnSurfaceVariant,
    outline = CosmicOutline
)

private val CoffeeColorScheme = lightColorScheme(
    primary = CoffeePrimary,
    onPrimary = CoffeeOnPrimary,
    primaryContainer = CoffeePrimaryContainer,
    onPrimaryContainer = CoffeeOnPrimaryContainer,
    secondary = CoffeePrimary,
    onSecondary = Color.White,
    background = CoffeeBackground,
    onBackground = CoffeeOnSurface,
    surface = CoffeeSurface,
    onSurface = CoffeeOnSurface,
    surfaceVariant = CoffeeSurfaceVariant,
    onSurfaceVariant = CoffeeOnSurfaceVariant,
    outline = CoffeeOutline
)

private val SunsetColorScheme = lightColorScheme(
    primary = SunsetPrimary,
    onPrimary = SunsetOnPrimary,
    primaryContainer = SunsetPrimaryContainer,
    onPrimaryContainer = SunsetOnPrimaryContainer,
    secondary = SunsetPrimary,
    onSecondary = Color.White,
    background = SunsetBackground,
    onBackground = SunsetOnSurface,
    surface = SunsetSurface,
    onSurface = SunsetOnSurface,
    surfaceVariant = SunsetSurfaceVariant,
    onSurfaceVariant = SunsetOnSurfaceVariant,
    outline = SunsetOutline
)

private val ForestColorScheme = lightColorScheme(
    primary = ForestPrimary,
    onPrimary = ForestOnPrimary,
    primaryContainer = ForestPrimaryContainer,
    onPrimaryContainer = ForestOnPrimaryContainer,
    secondary = ForestPrimary,
    onSecondary = Color.White,
    background = ForestBackground,
    onBackground = ForestOnSurface,
    surface = ForestSurface,
    onSurface = ForestOnSurface,
    surfaceVariant = ForestSurfaceVariant,
    onSurfaceVariant = ForestOnSurfaceVariant,
    outline = ForestOutline
)

private val TeddyColorScheme = lightColorScheme(
    primary = TeddyPrimary,
    onPrimary = TeddyOnPrimary,
    primaryContainer = TeddyPrimaryContainer,
    onPrimaryContainer = TeddyOnPrimaryContainer,
    secondary = TeddyPrimary,
    onSecondary = Color.White,
    background = TeddyBackground,
    onBackground = TeddyOnSurface,
    surface = TeddySurface,
    onSurface = TeddyOnSurface,
    surfaceVariant = TeddySurfaceVariant,
    onSurfaceVariant = TeddyOnSurfaceVariant,
    outline = TeddyOutline
)

@Composable
fun MemoraTheme(
    themeMode: String = "SYSTEM",
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        "DARK" -> true
        "LIGHT", "PINK", "BUNNY", "YELLOW", "HONEY", "KITTY", "MINT", "OCEAN", "COSMIC", "COFFEE", "SUNSET", "FOREST", "TEDDY" -> false
        else -> isSystemDark
    }

    val colorScheme = when (themeMode) {
        "PINK", "BUNNY" -> PinkColorScheme
        "YELLOW", "HONEY" -> YellowColorScheme
        "KITTY" -> KittyColorScheme
        "MINT" -> MintColorScheme
        "OCEAN" -> OceanColorScheme
        "COSMIC" -> CosmicColorScheme
        "COFFEE" -> CoffeeColorScheme
        "SUNSET" -> SunsetColorScheme
        "FOREST" -> ForestColorScheme
        "TEDDY" -> TeddyColorScheme
        "DARK" -> DarkColorScheme
        "LIGHT" -> LightColorScheme
        else -> {
            if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val context = LocalContext.current
                if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else if (isDark) {
                DarkColorScheme
            } else {
                LightColorScheme
            }
        }
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
