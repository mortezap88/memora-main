package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R

import androidx.compose.ui.graphics.luminance

/**
 * Aesthetic themed background with blurry ambient glowing canvas for pure Dark & Light modes,
 * and high-resolution art illustrations for special theme modes.
 */
@Composable
fun ThemedAppBackground(
    themeMode: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        "DARK" -> true
        "LIGHT" -> false
        "SYSTEM" -> isSystemDark
        else -> false
    }

    // Base background: In dark mode, deep cosmic slate; in light mode, soft radiant pastel ivory gradient
    val baseModifier = if (isDark) {
        Modifier.background(
            Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF0D101D),
                    Color(0xFF090B14),
                    Color(0xFF0F111E)
                )
            )
        )
    } else {
        Modifier.background(
            Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFFDF4FF), // soft lavender ivory
                    Color(0xFFF8FAFC), // pearl white
                    Color(0xFFEFF6FF), // soft celestial tint
                    Color(0xFFFDF2F8)  // soft rose tint
                )
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .then(baseModifier)
    ) {
        when (themeMode) {
            "PINK", "BUNNY" -> {
                Image(
                    painter = painterResource(id = R.drawable.img_bunny_bg_1787865904457),
                    contentDescription = "Cute Bunny Theme Background",
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(0.30f),
                    contentScale = ContentScale.Crop
                )
            }
            "KITTY" -> {
                Image(
                    painter = painterResource(id = R.drawable.img_kitty_bg_1787865917607),
                    contentDescription = "Cute Kitty Theme Background",
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(0.30f),
                    contentScale = ContentScale.Crop
                )
            }
            "YELLOW", "HONEY" -> {
                Image(
                    painter = painterResource(id = R.drawable.img_honey_bg_1787865930481),
                    contentDescription = "Honey Sunshine Theme Background",
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(0.30f),
                    contentScale = ContentScale.Crop
                )
            }
            "MINT" -> {
                Image(
                    painter = painterResource(id = R.drawable.img_mint_bg_1787865945841),
                    contentDescription = "Matcha Mint Theme Background",
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(0.30f),
                    contentScale = ContentScale.Crop
                )
            }
            "OCEAN" -> {
                Image(
                    painter = painterResource(id = R.drawable.img_ocean_bg_1787866688873),
                    contentDescription = "Ocean Breeze Theme Background",
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(0.30f),
                    contentScale = ContentScale.Crop
                )
            }
            "COSMIC" -> {
                Image(
                    painter = painterResource(id = R.drawable.img_cosmic_bg_1787866702226),
                    contentDescription = "Cosmic Dream Theme Background",
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(0.30f),
                    contentScale = ContentScale.Crop
                )
            }
            "COFFEE" -> {
                Image(
                    painter = painterResource(id = R.drawable.img_coffee_bg_1787866717293),
                    contentDescription = "Cozy Cafe Theme Background",
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(0.30f),
                    contentScale = ContentScale.Crop
                )
            }
            "SUNSET" -> {
                Image(
                    painter = painterResource(id = R.drawable.img_sunset_bg_1787866730249),
                    contentDescription = "Peach Sunset Theme Background",
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(0.30f),
                    contentScale = ContentScale.Crop
                )
            }
            "FOREST" -> {
                Image(
                    painter = painterResource(id = R.drawable.img_forest_bg_1787866764397),
                    contentDescription = "Enchanted Forest Theme Background",
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(0.30f),
                    contentScale = ContentScale.Crop
                )
            }
            "TEDDY" -> {
                Image(
                    painter = painterResource(id = R.drawable.img_teddy_bg_1787866776249),
                    contentDescription = "Teddy Bakery Theme Background",
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(0.30f),
                    contentScale = ContentScale.Crop
                )
            }
            else -> {
                // Vague & Blurry ambient colorful background orbs for Dark & Light modes
                AmbientGlowCanvas(isDark = isDark)
            }
        }

        content()
    }
}

/**
 * Draws soft, organic, vague blurred luminous orbs on the canvas for a modern glassmorphic feel.
 */
@Composable
fun AmbientGlowCanvas(
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        if (isDark) {
            // Top: Soft Crimson / Warm Ruby glow near top
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFE11D48).copy(alpha = 0.20f),
                        Color(0xFFEA580C).copy(alpha = 0.10f),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.20f, height * 0.12f),
                    radius = width * 0.65f
                ),
                radius = width * 0.65f,
                center = Offset(width * 0.20f, height * 0.12f)
            )

            // Upper-Right: Amber / Golden Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFF59E0B).copy(alpha = 0.20f),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.85f, height * 0.28f),
                    radius = width * 0.60f
                ),
                radius = width * 0.60f,
                center = Offset(width * 0.85f, height * 0.28f)
            )

            // Middle: Emerald & Aqua Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF10B981).copy(alpha = 0.16f),
                        Color(0xFF06B6D4).copy(alpha = 0.10f),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.15f, height * 0.48f),
                    radius = width * 0.65f
                ),
                radius = width * 0.65f,
                center = Offset(width * 0.15f, height * 0.48f)
            )

            // Lower-Middle: Indigo / Cobalt Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF6366F1).copy(alpha = 0.22f),
                        Color(0xFF8B5CF6).copy(alpha = 0.10f),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.85f, height * 0.68f),
                    radius = width * 0.70f
                ),
                radius = width * 0.70f,
                center = Offset(width * 0.85f, height * 0.68f)
            )

            // Bottom-Left: Vivid Purple / Violet Glow behind Bottom Bar
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFA855F7).copy(alpha = 0.24f),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.15f, height * 0.94f),
                    radius = width * 0.55f
                ),
                radius = width * 0.55f,
                center = Offset(width * 0.15f, height * 0.94f)
            )

            // Bottom-Right: Warm Amber / Golden Glow behind Settings Tab
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFF59E0B).copy(alpha = 0.24f),
                        Color(0xFFEA580C).copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.88f, height * 0.94f),
                    radius = width * 0.55f
                ),
                radius = width * 0.55f,
                center = Offset(width * 0.88f, height * 0.94f)
            )
        } else {
            // Light Mode: Dreamy, Colorful Diffused Ambient Rainbow/Fades Aura across the screen
            // 1. Top-Left: Warm Strawberry / Ruby Rose Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFDA4AF).copy(alpha = 0.38f),
                        Color(0xFFFECDD3).copy(alpha = 0.18f),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.18f, height * 0.10f),
                    radius = width * 0.75f
                ),
                radius = width * 0.75f,
                center = Offset(width * 0.18f, height * 0.10f)
            )

            // 2. Upper-Right: Glowing Peach Amber / Honey Aura
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFDE68A).copy(alpha = 0.40f),
                        Color(0xFFFED7AA).copy(alpha = 0.20f),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.86f, height * 0.25f),
                    radius = width * 0.70f
                ),
                radius = width * 0.70f,
                center = Offset(width * 0.86f, height * 0.25f)
            )

            // 3. Middle-Left: Minty Emerald & Aqua Marine Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFA7F3D0).copy(alpha = 0.35f),
                        Color(0xFFBAE6FD).copy(alpha = 0.20f),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.12f, height * 0.46f),
                    radius = width * 0.70f
                ),
                radius = width * 0.70f,
                center = Offset(width * 0.12f, height * 0.46f)
            )

            // 4. Middle-Right: Sky Azure / Cyan Breeze
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF93C5FD).copy(alpha = 0.38f),
                        Color(0xFFC7D2FE).copy(alpha = 0.20f),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.88f, height * 0.62f),
                    radius = width * 0.75f
                ),
                radius = width * 0.75f,
                center = Offset(width * 0.88f, height * 0.62f)
            )

            // 5. Lower-Left: Luminous Lilac / Purple Violet
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFDDD6FE).copy(alpha = 0.42f),
                        Color(0xFFE9D5FF).copy(alpha = 0.22f),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.20f, height * 0.88f),
                    radius = width * 0.70f
                ),
                radius = width * 0.70f,
                center = Offset(width * 0.20f, height * 0.88f)
            )

            // 6. Lower-Right: Warm Amber Sunset Glow behind bottom bar
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFDE047).copy(alpha = 0.35f),
                        Color(0xFFFBCFE8).copy(alpha = 0.20f),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.82f, height * 0.92f),
                    radius = width * 0.60f
                ),
                radius = width * 0.60f,
                center = Offset(width * 0.82f, height * 0.92f)
            )
        }
    }
}

