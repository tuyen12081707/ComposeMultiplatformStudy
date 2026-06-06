package com.panda.study1.web

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ------- Palette -------
private val DeepNavy        = Color(0xFF0F0F14)
private val SurfaceDark     = Color(0xFF1A1A26)
private val CardDark        = Color(0xFF22223A)
private val AccentViolet    = Color(0xFF8B5CF6)   // vibrant purple
private val AccentVioletDim = Color(0xFF6D28D9)
private val NeonCyan        = Color(0xFF22D3EE)
private val TextPrimary     = Color(0xFFF1F5F9)
private val TextSecondary   = Color(0xFFCBD5E1)
private val TextMuted       = Color(0xFF64748B)

private val XapkDarkColors = darkColorScheme(
    primary          = AccentViolet,
    onPrimary        = Color.White,
    primaryContainer = AccentVioletDim,
    secondary        = NeonCyan,
    onSecondary      = DeepNavy,
    background       = DeepNavy,
    onBackground     = TextPrimary,
    surface          = SurfaceDark,
    onSurface        = TextPrimary,
    surfaceVariant   = CardDark,
    onSurfaceVariant = TextSecondary,
    outline          = TextMuted,
)

@Composable
fun XapkTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = XapkDarkColors,
        content     = content
    )
}
