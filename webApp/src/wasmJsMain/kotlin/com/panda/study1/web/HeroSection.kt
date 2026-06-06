package com.panda.study1.web

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HeroSection() {
    val infiniteTransition = rememberInfiniteTransition(label = "hero_pulse")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue  = 1.15f,
        animationSpec = infiniteRepeatable(
            animation  = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A1032),
                        Color(0xFF0F0F14)
                    )
                )
            )
            .padding(top = 72.dp, bottom = 56.dp, start = 24.dp, end = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        // Decorative glow blob behind logo
        Box(
            modifier = Modifier
                .size(200.dp)
                .scale(glowScale)
                .blur(80.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0x558B5CF6), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // App icon
            AppIcon()

            Spacer(Modifier.height(28.dp))

            // Headline
            Text(
                text       = "XAPK Installer",
                style      = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize   = 52.sp,
                    brush      = Brush.horizontalGradient(
                        colors = listOf(Color(0xFFE0C3FC), Color(0xFF8B5CF6), Color(0xFF22D3EE))
                    )
                ),
                textAlign  = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            // Badge
            Box(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.extraLarge)
                    .background(Color(0xFF8B5CF6).copy(alpha = 0.18f))
                    .border(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.45f), MaterialTheme.shapes.extraLarge)
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text  = "by Ice Bear Studio",
                    color = Color(0xFF8B5CF6),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }

            Spacer(Modifier.height(20.dp))

            // Subtitle
            Text(
                text      = "The easiest way to install Android Split APKs\nto your device faking Google Play source.",
                style     = MaterialTheme.typography.titleMedium.copy(
                    color      = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 28.sp
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun AppIcon() {
    Box(
        modifier = Modifier
            .size(96.dp)
            .clip(MaterialTheme.shapes.extraLarge)
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF6D28D9), Color(0xFF22D3EE))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text  = "📦",
            fontSize = 46.sp
        )
    }
}
