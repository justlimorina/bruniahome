package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import com.example.data.model.AppThemeMode
import com.example.data.model.MonetPalette

val PixelShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

private fun getPaletteColorScheme(palette: MonetPalette, darkTheme: Boolean): ColorScheme {
    return when (palette) {
        MonetPalette.TONAL_SPOT -> {
            if (darkTheme) {
                darkColorScheme(
                    primary = BotanicalPrimaryDark,
                    onPrimary = BotanicalOnPrimaryDark,
                    primaryContainer = BotanicalPrimaryContainerDark,
                    onPrimaryContainer = BotanicalOnPrimaryContainerDark,
                    secondary = BotanicalSecondaryDark,
                    secondaryContainer = BotanicalSecondaryContainerDark,
                    surface = BotanicalSurfaceDark,
                    surfaceVariant = BotanicalSurfaceVariantDark,
                    background = BotanicalBackgroundDark
                )
            } else {
                lightColorScheme(
                    primary = BotanicalPrimaryLight,
                    onPrimary = BotanicalOnPrimaryLight,
                    primaryContainer = BotanicalPrimaryContainerLight,
                    onPrimaryContainer = BotanicalOnPrimaryContainerLight,
                    secondary = BotanicalSecondaryLight,
                    secondaryContainer = BotanicalSecondaryContainerLight,
                    surface = BotanicalSurfaceLight,
                    surfaceVariant = BotanicalSurfaceVariantLight,
                    background = BotanicalBackgroundLight
                )
            }
        }
        MonetPalette.VIBRANT -> {
            if (darkTheme) {
                darkColorScheme(
                    primary = OceanPrimaryDark,
                    primaryContainer = OceanPrimaryContainerDark,
                    surface = OceanSurfaceDark,
                    background = OceanSurfaceDark
                )
            } else {
                lightColorScheme(
                    primary = OceanPrimaryLight,
                    primaryContainer = OceanPrimaryContainerLight,
                    secondary = OceanSecondaryLight,
                    surface = OceanSurfaceLight,
                    background = OceanSurfaceLight
                )
            }
        }
        MonetPalette.EXPRESSIVE -> {
            if (darkTheme) {
                darkColorScheme(
                    primary = LavenderPrimaryDark,
                    primaryContainer = LavenderPrimaryContainerDark,
                    surface = LavenderSurfaceDark,
                    background = LavenderSurfaceDark
                )
            } else {
                lightColorScheme(
                    primary = LavenderPrimaryLight,
                    primaryContainer = LavenderPrimaryContainerLight,
                    secondary = LavenderSecondaryLight,
                    surface = LavenderSurfaceLight,
                    background = LavenderSurfaceLight
                )
            }
        }
        MonetPalette.SPRITZ -> {
            if (darkTheme) {
                darkColorScheme(
                    primary = AmberPrimaryDark,
                    primaryContainer = AmberPrimaryContainerDark,
                    surface = AmberSurfaceDark,
                    background = AmberSurfaceDark
                )
            } else {
                lightColorScheme(
                    primary = AmberPrimaryLight,
                    primaryContainer = AmberPrimaryContainerLight,
                    secondary = AmberSecondaryLight,
                    surface = AmberSurfaceLight,
                    background = AmberSurfaceLight
                )
            }
        }
        MonetPalette.FRUIT_SALAD -> {
            if (darkTheme) {
                darkColorScheme(
                    primary = Color(0xFFFFB4A1),
                    primaryContainer = Color(0xFF7C2C16),
                    surface = Color(0xFF201A18),
                    background = Color(0xFF201A18)
                )
            } else {
                lightColorScheme(
                    primary = Color(0xFF994326),
                    primaryContainer = Color(0xFFFFDBD1),
                    surface = Color(0xFFFFF8F6),
                    background = Color(0xFFFFF8F6)
                )
            }
        }
    }
}

@Composable
fun BruniaHomeTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    dynamicColor: Boolean = true,
    monetPalette: MonetPalette = MonetPalette.TONAL_SPOT,
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        AppThemeMode.SYSTEM -> systemInDark
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> getPaletteColorScheme(monetPalette, isDark)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = PixelShapes,
        content = content
    )
}
