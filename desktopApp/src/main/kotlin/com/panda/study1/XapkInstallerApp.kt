package com.panda.study1

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragData
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.ui.draganddrop.dragData
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.net.URI

// ── Color palette ────────────────────────────────────────────────────────────
private val BgDeep        = Color(0xFF0D1117)
private val BgSurface     = Color(0xFF161B22)
private val BgCard        = Color(0xFF1E2430)
private val AccentGreen   = Color(0xFF3FB950)
private val AccentBlue    = Color(0xFF58A6FF)
private val AccentOrange  = Color(0xFFFF9500)
private val AccentRed     = Color(0xFFF85149)
private val AccentPurple  = Color(0xFF8B5CF6)
private val TextPrimary   = Color(0xFFE6EDF3)
private val TextSecondary = Color(0xFF8B949E)
private val BorderColor   = Color(0xFF30363D)

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun InstallerState.accentColor() = when (this) {
    is InstallerState.Idle       -> AccentBlue
    is InstallerState.Extracting -> AccentOrange
    is InstallerState.Installing -> AccentPurple
    is InstallerState.Success    -> AccentGreen
    is InstallerState.Error      -> AccentRed
}

private fun InstallerState.icon() = when (this) {
    is InstallerState.Idle       -> "📦"
    is InstallerState.Extracting -> "📂"
    is InstallerState.Installing -> "🚀"
    is InstallerState.Success    -> "✅"
    is InstallerState.Error      -> "❌"
}

private fun InstallerState.label() = when (this) {
    is InstallerState.Idle       -> "Drop an XAPK, APKS, or APK folder here"
    is InstallerState.Extracting -> "Extracting  ${this.fileName}…"
    is InstallerState.Installing -> "Installing ${this.apkCount} APK(s) via ADB…"
    is InstallerState.Success    -> this.message
    is InstallerState.Error      -> this.message
}

// ── Root composable ───────────────────────────────────────────────────────────

@Composable
fun XapkInstallerApp(viewModel: InstallerViewModel = remember { InstallerViewModel() }) {
    val state  by viewModel.state.collectAsState()
    val logs   by viewModel.logs.collectAsState()

    MaterialTheme(colorScheme = darkColorScheme(background = BgDeep, surface = BgSurface)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BgDeep)
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── Header ────────────────────────────────────────────────────
                AppHeader()

                // ── Drop zone ─────────────────────────────────────────────────
                DropZone(
                    state = state,
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    onDrop = { path -> viewModel.handleDrop(path) }
                )

                // ── Log console ───────────────────────────────────────────────
                LogConsole(
                    logs = logs,
                    modifier = Modifier.fillMaxWidth().weight(1f)
                )

                // ── Bottom bar ────────────────────────────────────────────────
                BottomBar(state = state, onReset = viewModel::reset)
            }
        }
    }
}

// ── Header ────────────────────────────────────────────────────────────────────

@Composable
private fun AppHeader() {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("📲", fontSize = 28.sp)
        Column {
            Text(
                text = "XAPK Installer",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                letterSpacing = 0.5.sp
            )
            Text(
                text = "Installs via ADB · Fakes Google Play source",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
    }
    HorizontalDivider(color = BorderColor, thickness = 1.dp)
}

// ── Drop zone ────────────────────────────────────────────────────────────────

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun DropZone(
    state: InstallerState,
    modifier: Modifier = Modifier,
    onDrop: (String) -> Unit
) {
    var isDraggingOver by remember { mutableStateOf(false) }
    val accent = state.accentColor()
    val isBusy = state is InstallerState.Extracting || state is InstallerState.Installing

    // Pulsing border animation while busy
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 1f, label = "borderAlpha",
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse)
    )

    val effectiveBorderColor = when {
        isDraggingOver -> AccentBlue
        isBusy         -> accent.copy(alpha = borderAlpha)
        else           -> accent.copy(alpha = 0.5f)
    }
    val bgColor = if (isDraggingOver)
        AccentBlue.copy(alpha = 0.08f)
    else
        accent.copy(alpha = 0.04f)

    val dropTarget = remember(isBusy, onDrop) {
        object : DragAndDropTarget {
            override fun onEntered(event: DragAndDropEvent) {
                isDraggingOver = true
            }
            override fun onExited(event: DragAndDropEvent) {
                isDraggingOver = false
            }
            override fun onEnded(event: DragAndDropEvent) {
                isDraggingOver = false
            }
            override fun onDrop(event: DragAndDropEvent): Boolean {
                isDraggingOver = false
                if (isBusy) return false
                val dragData = event.dragData()
                if (dragData is DragData.FilesList) {
                    dragData.readFiles().firstOrNull()?.let { fileUri ->
                        runCatching {
                            // Paths.get(URI) fully decodes percent-encoding (%20 → space, etc.)
                            // and returns the correct native path on every OS.
                            val path = java.nio.file.Paths.get(URI(fileUri)).toString()
                            onDrop(path)
                        }
                    }
                }
                return true
            }
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .drawBehind {
                drawRoundRect(
                    color = effectiveBorderColor,
                    cornerRadius = CornerRadius(16.dp.toPx()),
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f))
                    )
                )
            }
            .dragAndDropTarget(
                shouldStartDragAndDrop = { !isBusy },
                target = dropTarget
            )
    ) {
        DropZoneContent(state = state, isDraggingOver = isDraggingOver)
    }
}

@Composable
private fun DropZoneContent(state: InstallerState, isDraggingOver: Boolean) {
    val isBusy = state is InstallerState.Extracting || state is InstallerState.Installing

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Big icon with spin animation when busy
        if (isBusy) {
            val rotation by rememberInfiniteTransition(label = "spin").animateFloat(
                initialValue = 0f, targetValue = 360f, label = "rotation",
                animationSpec = infiniteRepeatable(tween(1500, easing = LinearEasing))
            )
            Text(
                text = "⚙️",
                fontSize = 36.sp,
                modifier = Modifier.graphicsLayer(rotationZ = rotation)
            )
        } else {
            Text(text = if (isDraggingOver) "🎯" else state.icon(), fontSize = 36.sp)
        }

        Text(
            text = if (isDraggingOver) "Release to install!" else state.label(),
            color = if (isDraggingOver) AccentBlue else state.accentColor(),
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )

        if (state is InstallerState.Idle) {
            Text(
                text = "Supports: .xapk  •  .apks  •  APK folder",
                color = TextSecondary,
                fontSize = 11.sp
            )
        }
    }
}

// ── Log console ───────────────────────────────────────────────────────────────

@Composable
private fun LogConsole(logs: List<String>, modifier: Modifier = Modifier) {
    val listState = rememberLazyListState()

    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) listState.animateScrollToItem(logs.lastIndex)
    }

    Column(modifier = modifier) {
        // Console title bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BgCard, RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(Modifier.size(10.dp).background(AccentRed,   RoundedCornerShape(50)))
            Box(Modifier.size(10.dp).background(AccentOrange, RoundedCornerShape(50)))
            Box(Modifier.size(10.dp).background(AccentGreen,  RoundedCornerShape(50)))
            Spacer(Modifier.width(8.dp))
            Text("ADB Output Console", color = TextSecondary, fontSize = 12.sp)
        }

        // Console body
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0A0E14), RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                .border(1.dp, BorderColor, RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                .padding(12.dp)
        ) {
            if (logs.isEmpty()) {
                Text(
                    text = "// Logs will appear here once you drop a file…",
                    color = TextSecondary.copy(alpha = 0.5f),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(state = listState, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    items(logs) { line ->
                        Text(
                            text = line,
                            color = logLineColor(line),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

private fun logLineColor(line: String): Color = when {
    line.startsWith("✅") || line.contains("Success", ignoreCase = true) -> AccentGreen
    line.startsWith("❌") || line.startsWith("ERROR") || line.startsWith("EXCEPTION") -> AccentRed
    line.startsWith("🚀") || line.startsWith("$") -> AccentBlue
    line.startsWith("📦") || line.startsWith("📂") -> AccentOrange
    line.startsWith("🗑️") || line.startsWith("🔍") -> AccentPurple
    else -> TextPrimary.copy(alpha = 0.85f)
}

// ── Bottom bar ────────────────────────────────────────────────────────────────

@Composable
private fun BottomBar(state: InstallerState, onReset: () -> Unit) {
    val canReset = state is InstallerState.Success || state is InstallerState.Error

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Status chip
        AnimatedContent(
            targetState = state,
            transitionSpec = { fadeIn() + slideInVertically { it } togetherWith fadeOut() },
            label = "statusChip"
        ) { s ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .background(s.accentColor().copy(alpha = 0.12f), RoundedCornerShape(50))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Box(Modifier.size(7.dp).background(s.accentColor(), RoundedCornerShape(50)))
                Text(
                    text = s::class.simpleName ?: "",
                    color = s.accentColor(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        if (canReset) {
            TextButton(onClick = onReset) {
                Text("Reset", color = TextSecondary, fontSize = 12.sp)
            }
        }
    }
}
