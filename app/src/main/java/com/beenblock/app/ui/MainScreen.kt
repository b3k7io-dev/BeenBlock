package com.beenblock.app.ui

import android.content.Intent
import android.graphics.RenderEffect
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatterySaver
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.beenblock.app.R
import com.beenblock.app.data.AppInfo

private data class GlassStyle(
    val fill: Color,
    val fillStrong: Color,
    val stroke: Color,
    val highlight: Color,
)

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshSystemStatus()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val glass = if (dark) {
        GlassStyle(
            fill = Color(0x22FFFFFF),
            fillStrong = Color(0x30FFFFFF),
            stroke = Color(0x33FFFFFF),
            highlight = Color(0x14FFFFFF),
        )
    } else {
        GlassStyle(
            fill = Color(0x66FFFFFF),
            fillStrong = Color(0x99FFFFFF),
            stroke = Color(0x26000000),
            highlight = Color(0x55FFFFFF),
        )
    }
    val bg = if (dark) {
        Brush.verticalGradient(listOf(Color(0xFF050506), Color(0xFF0B0B0E), Color(0xFF111116)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFF7F7F8), Color(0xFFEFEFF2), Color(0xFFF3F3F5)))
    }

    val setupComplete = state.accessibilityEnabled && state.batteryOptimizationIgnored

    Box(modifier = Modifier.fillMaxSize().background(bg)) {
        LiquidBackground(dark = dark)

        if (!setupComplete) {
            SetupGateScreen(
                state = state,
                glass = glass,
                onOpenAccessibility = {
                    runCatching { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
                },
                onOpenBattery = {
                    val pkgUri = Uri.parse("package:${context.packageName}")
                    val direct = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, pkgUri)
                    runCatching { context.startActivity(direct) }.onFailure {
                        runCatching { context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)) }
                    }
                },
            )
        } else {
            AppListScreen(
                state = state,
                glass = glass,
                onQueryChange = viewModel::onQueryChange,
                onToggleBlocked = viewModel::toggleBlocked,
            )
        }
    }
}

@Composable
private fun LiquidBackground(dark: Boolean) {
    val blur = rememberBlurModifier(48f)
    val orbA = if (dark) Color(0x24FFFFFF) else Color(0x18000000)
    val orbB = if (dark) Color(0x18FFFFFF) else Color(0x12000000)
    Box(Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .padding(top = 54.dp, start = 150.dp)
                .size(300.dp)
                .then(blur)
                .background(Brush.radialGradient(listOf(orbA, Color.Transparent)), CircleShape)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 120.dp, end = 10.dp)
                .size(260.dp)
                .then(blur)
                .background(Brush.radialGradient(listOf(orbB, Color.Transparent)), CircleShape)
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 260.dp)
                .size(180.dp)
                .then(rememberBlurModifier(36f))
                .background(Brush.radialGradient(listOf(orbB, Color.Transparent)), CircleShape)
        )
    }
}

@Composable
private fun rememberBlurModifier(radius: Float): Modifier {
    return remember(radius) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Modifier.graphicsLayer {
                renderEffect = RenderEffect.createBlurEffect(radius, radius, Shader.TileMode.DECAL)
                    .asComposeRenderEffect()
            }
        } else Modifier
    }
}

@Composable
private fun SetupGateScreen(
    state: MainUiState,
    glass: GlassStyle,
    onOpenAccessibility: () -> Unit,
    onOpenBattery: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp, end = 16.dp,
            top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 18.dp,
            bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 28.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            GlassCard(glass = glass, emphasized = true) {
                Column(Modifier.padding(22.dp)) {
                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f),
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                                    )
                                )
                            )
                            .border(1.dp, glass.stroke, RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Rounded.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(30.dp))
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(stringResource(R.string.setup_title), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.height(6.dp))
                    Text(stringResource(R.string.setup_subtitle), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            SetupStepCard(
                index = 1,
                icon = Icons.Rounded.Security,
                title = stringResource(R.string.accessibility_title),
                description = stringResource(if (state.accessibilityEnabled) R.string.accessibility_enabled else R.string.accessibility_disabled),
                done = state.accessibilityEnabled,
                actionLabel = stringResource(R.string.open_accessibility),
                glass = glass,
                onAction = onOpenAccessibility,
            )
        }
        item {
            SetupStepCard(
                index = 2,
                icon = Icons.Rounded.BatterySaver,
                title = stringResource(R.string.battery_title),
                description = stringResource(if (state.batteryOptimizationIgnored) R.string.battery_ok else R.string.battery_limited),
                done = state.batteryOptimizationIgnored,
                actionLabel = stringResource(R.string.open_battery),
                glass = glass,
                onAction = onOpenBattery,
            )
        }
        item {
            GlassCard(glass = glass) {
                Text(
                    stringResource(R.string.setup_footer),
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SetupStepCard(
    index: Int,
    icon: ImageVector,
    title: String,
    description: String,
    done: Boolean,
    actionLabel: String,
    glass: GlassStyle,
    onAction: () -> Unit,
) {
    val border by animateColorAsState(
        if (done) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.34f) else glass.stroke,
        animationSpec = tween(280, easing = FastOutSlowInEasing), label = "stepBorder"
    )
    val dotBg by animateColorAsState(
        if (done) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surfaceVariant,
        label = "dotBg"
    )
    val dotFg by animateColorAsState(
        if (done) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "dotFg"
    )

    GlassCard(glass = glass, border = border) {
        Column(Modifier.fillMaxWidth().animateContentSize(spring(stiffness = Spring.StiffnessMediumLow)).padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(34.dp).background(dotBg, CircleShape), contentAlignment = Alignment.Center) {
                    if (done) Icon(Icons.Rounded.Check, null, tint = dotFg, modifier = Modifier.size(18.dp))
                    else Text("$index", color = dotFg, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(10.dp))
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
            Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            GlassActionButton(
                label = if (done) stringResource(R.string.check_again) else actionLabel,
                primary = !done,
                onClick = onAction,
            )
        }
    }
}

@Composable
private fun GlassActionButton(label: String, primary: Boolean, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.985f else 1f, spring(stiffness = Spring.StiffnessMedium), label = "btnScale")
    Button(
        onClick = onClick,
        interactionSource = interaction,
        modifier = Modifier.scale(scale),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (primary) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f),
            contentColor = if (primary) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurface,
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp),
    ) {
        Text(label, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.width(6.dp))
        Icon(Icons.Rounded.ChevronRight, contentDescription = null, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun AppListScreen(
    state: MainUiState,
    glass: GlassStyle,
    onQueryChange: (String) -> Unit,
    onToggleBlocked: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp, end = 16.dp,
            top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 14.dp,
            bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "header") { Header(state = state, glass = glass) }
        item(key = "search") {
            OutlinedTextField(
                value = state.query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                placeholder = { Text(stringResource(R.string.search_apps), color = MaterialTheme.colorScheme.onSurfaceVariant) },
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = glass.fillStrong,
                    unfocusedContainerColor = glass.fill,
                    focusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.28f),
                    unfocusedBorderColor = glass.stroke,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    cursorColor = MaterialTheme.colorScheme.onSurface,
                    focusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        }

        if (state.loading) {
            item(key = "loading") {
                GlassCard(glass = glass) {
                    Row(
                        Modifier.fillMaxWidth().padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.width(12.dp))
                        Text(stringResource(R.string.loading_apps), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        } else if (state.apps.isEmpty()) {
            item(key = "empty") {
                GlassCard(glass = glass) {
                    Text(stringResource(R.string.no_apps_found), modifier = Modifier.padding(20.dp), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            items(state.apps, key = { it.packageName }, contentType = { "app" }) { app ->
                AppRow(
                    app = app,
                    glass = glass,
                    onToggle = { onToggleBlocked(app.packageName) },
                    modifier = Modifier.animateItem(
                        fadeInSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        placementSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
                        fadeOutSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    ),
                )
            }
        }
    }
}

@Composable
private fun Header(state: MainUiState, glass: GlassStyle) {
    GlassCard(glass = glass, emphasized = true) {
        Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(17.dp))
                    .background(Brush.linearGradient(listOf(Color(0x22FFFFFF), Color(0x08FFFFFF))))
                    .border(1.dp, glass.stroke, RoundedCornerShape(17.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(stringResource(R.string.blocked_count, state.blockedCount), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f))
                    .border(1.dp, glass.stroke, RoundedCornerShape(999.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(stringResource(R.string.status_active), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun AppRow(
    app: AppInfo,
    glass: GlassStyle,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.992f else 1f, spring(stiffness = Spring.StiffnessMediumLow), label = "rowScale")
    val border by animateColorAsState(
        if (app.isBlocked) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f) else glass.stroke,
        label = "rowBorder"
    )
    val fill by animateColorAsState(if (app.isBlocked) glass.fillStrong else glass.fill, label = "rowFill")
    val iconPainter = remember(app.packageName, app.icon) { app.icon?.let { BitmapPainter(it) } }

    GlassCard(glass = glass, border = border, fill = fill, modifier = modifier.scale(scale)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(interactionSource = interaction, indication = null, onClick = onToggle)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    .border(1.dp, glass.stroke, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                if (iconPainter != null) {
                    Image(painter = iconPainter, contentDescription = null, modifier = Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)))
                } else {
                    Icon(Icons.Rounded.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(app.label, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(app.packageName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.width(8.dp))
            Switch(
                checked = app.isBlocked,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.background,
                    checkedTrackColor = MaterialTheme.colorScheme.onSurface,
                    checkedBorderColor = MaterialTheme.colorScheme.onSurface,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurface,
                    uncheckedTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    uncheckedBorderColor = glass.stroke,
                ),
            )
        }
    }
}

@Composable
private fun GlassCard(
    glass: GlassStyle,
    modifier: Modifier = Modifier,
    emphasized: Boolean = false,
    border: Color = glass.stroke,
    fill: Color = if (emphasized) glass.fillStrong else glass.fill,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(24.dp)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(glass.highlight, Color.Transparent, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.02f))
                ),
                shape
            )
            .border(1.dp, border, shape),
        shape = shape,
        color = fill,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        content = content,
    )
}

private fun Color.luminance(): Float = (0.299f * red + 0.587f * green + 0.114f * blue)
