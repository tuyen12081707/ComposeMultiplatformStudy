package com.panda.study1.web

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val BASE = "https://github.com/tuyen12081707/ComposeMultiplatformStudy/releases/download/1.0.2"

private data class PlatformInfo(
    val icon: String,
    val label: String,
    val sublabel: String,
    val extension: String,
    val accentStart: Color,
    val accentEnd: Color,
    val url: String
)

private val platforms = listOf(
    PlatformInfo(
        icon        = "🍎",
        label       = "macOS",
        sublabel    = "Apple Silicon & Intel",
        extension   = ".dmg",
        accentStart = Color(0xFF6366F1),
        accentEnd   = Color(0xFF8B5CF6),
        url         = "$BASE/XAPK.Installer-1.0.1.dmg"
    ),
    PlatformInfo(
        icon        = "🪟",
        label       = "Windows",
        sublabel    = "Windows 10 / 11",
        extension   = ".msi",
        accentStart = Color(0xFF0EA5E9),
        accentEnd   = Color(0xFF22D3EE),
        url         = "$BASE/xapk_installer_window-1.0.1.uu"
    ),
    PlatformInfo(
        icon        = "🐧",
        label       = "Ubuntu",
        sublabel    = "Debian-based distros",
        extension   = ".deb",
        accentStart = Color(0xFFE8541A),
        accentEnd   = Color(0xFFF59E0B),
        url         = "$BASE/xapk_installer_ubutu-1.0.1.deb"
    ),
)

@Composable
fun DownloadSection() {
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Section label
        Text(
            text  = "⬇  Download Now",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onBackground
            )
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text  = "Free & open source. Pick your platform.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.outline
            ),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(36.dp))

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val isWide = maxWidth >= 800.dp

            if (isWide) {
                Row(
                    modifier            = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally),
                    verticalAlignment   = Alignment.Top
                ) {
                    platforms.forEach { platform ->
                        DownloadCard(
                            platform = platform,
                            modifier = Modifier.weight(1f).widthIn(max = 280.dp),
                            onClick  = { uriHandler.openUri(platform.url) }
                        )
                    }
                }
            } else {
                Column(
                    modifier              = Modifier.fillMaxWidth(),
                    verticalArrangement   = Arrangement.spacedBy(16.dp),
                    horizontalAlignment   = Alignment.CenterHorizontally
                ) {
                    platforms.forEach { platform ->
                        DownloadCard(
                            platform = platform,
                            modifier = Modifier.fillMaxWidth(),
                            onClick  = { uriHandler.openUri(platform.url) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DownloadCard(
    platform: PlatformInfo,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()

    val scale by animateFloatAsState(
        targetValue = if (hovered) 1.04f else 1f,
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "card_scale"
    )

    Column(
        modifier = modifier
            .scale(scale)
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(
                width = if (hovered) 2.dp else 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(platform.accentStart, platform.accentEnd)
                ),
                shape = MaterialTheme.shapes.large
            )
            .hoverable(interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication        = null,
                onClick           = onClick
            )
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Platform emoji icon
        Text(text = platform.icon, fontSize = 48.sp)

        Spacer(Modifier.height(16.dp))

        // Platform name
        Text(
            text  = platform.label,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onSurface
            )
        )

        Spacer(Modifier.height(4.dp))

        // Sub-label
        Text(
            text  = platform.sublabel,
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.outline
            )
        )

        Spacer(Modifier.height(24.dp))

        // Download button
        Row(
            modifier = Modifier
                .clip(MaterialTheme.shapes.medium)
                .background(
                    Brush.linearGradient(
                        colors = listOf(platform.accentStart, platform.accentEnd)
                    )
                )
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text  = "Download",
                style = MaterialTheme.typography.labelLarge.copy(
                    color      = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text  = platform.extension,
                style = MaterialTheme.typography.labelLarge.copy(
                    color      = Color.White.copy(alpha = 0.75f),
                    fontWeight = FontWeight.Normal
                )
            )
        }
    }
}
