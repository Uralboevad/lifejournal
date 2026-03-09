package com.example.lifejournal.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val JournalColorScheme = lightColorScheme(
    primary          = Coral,
    onPrimary        = CardWhite,
    primaryContainer = CoralLight,
    secondary        = Lavender,
    onSecondary      = CardWhite,
    secondaryContainer = LavenderLight,
    tertiary         = Mint,
    background       = CreamWhite,
    surface          = CreamSurface,
    onBackground     = DarkBrown,
    onSurface        = DarkBrown,
    outline          = WarmGrey
)

val JournalTypography = Typography(
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize   = 28.sp,
        color      = DarkBrown
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize   = 22.sp,
        color      = DarkBrown
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize   = 18.sp,
        color      = DarkBrown
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize   = 16.sp,
        color      = DarkBrown
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize   = 14.sp,
        color      = WarmGrey
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize   = 11.sp,
        color      = WarmGrey
    )
)

@Composable
fun LifeJournalTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = JournalColorScheme,
        typography  = JournalTypography,
        content     = content
    )
}