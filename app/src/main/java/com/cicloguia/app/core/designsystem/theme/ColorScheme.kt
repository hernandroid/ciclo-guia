package com.cicloguia.app.core.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val LightColorScheme = lightColorScheme(
    primary = BrandColors.Primary,
    onPrimary = Color.White,
    primaryContainer = BrandColors.PrimaryLight,
    onPrimaryContainer = BrandColors.PrimaryDark,

    background = BrandColors.BackgroundLight,
    onBackground = Color(0xFF102019),

    surface = BrandColors.SurfaceLight,
    onSurface = Color(0xFF102019),

    surfaceVariant = BrandColors.SurfaceVariantLight,
    onSurfaceVariant = Color(0xFF385247),

    outline = BrandColors.OutlineLight,

    error = BrandColors.Error,
    onError = Color.White
)

val DarkColorScheme = darkColorScheme(
    primary = BrandColors.Primary,
    onPrimary = Color.White,
    primaryContainer = BrandColors.PrimaryDark,
    onPrimaryContainer = BrandColors.PrimaryLight,

    background = BrandColors.BackgroundDark,
    onBackground = Color(0xFFE6F5EE),

    surface = BrandColors.SurfaceDark,
    onSurface = Color(0xFFE6F5EE),

    surfaceVariant = BrandColors.SurfaceVariantDark,
    onSurfaceVariant = Color(0xFFB8D6C9),

    outline = BrandColors.OutlineDark,

    error = BrandColors.Error,
    onError = Color.White
)