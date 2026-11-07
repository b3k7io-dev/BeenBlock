package com.beenblock.app.ui

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Shield
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.beenblock.app.R
import com.beenblock.app.data.AppInfo
import com.beenblock.app.ui.theme.AccentAmber
import com.beenblock.app.ui.theme.AccentBlue
import com.beenblock.app.ui.theme.AccentCyan
import com.beenblock.app.ui.theme.AccentGreen
import com.beenblock.app.ui.theme.AccentRose
import com.beenblock.app.ui.theme.AccentViolet
import com.beenblock.app.ui.theme.GlassDark
import com.beenblock.app.ui.theme.GlassDarkBorder
import com.beenblock.app.ui.theme.GlassLight
import com.beenblock.app.ui.theme.GlassLightBorder

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
    val glass = if (dark) GlassDark else GlassLight
    val glassBorder = if (dark) GlassDarkBorder else GlassLightBorder
    val bgBrush = if (dark) {
        Brush.verticalGradient(
            listOf(Color(0xFF0B1020), Color(0xFF111A33), Color(0xFF0E1428))
        )
    } else {
        Brush.verticalGradient(
            listOf(Color(0xFFF7FAFF), Color(0xFFEDF3FF), Color(0xFFF4F7FF))
        )
    }
    val orbBrush = Brush.radialGradient(
        listOf(AccentBlue.copy(alpha = if (dark) 0.22f else 0.18f), Color.Transparent)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgBrush)
    ) {
        Box(
            modifier = Modifier
                .padding(top = 40.dp, start = 180.dp)
                .size(260.dp)
                .background(orbBrush, CircleShape)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 120.dp)
                .size(220.dp)
                .background(
                    Brush.radialGradient(
                        listOf(AccentViolet.copy(alpha = if (dark) 0.18f else 0.14f), Color.Transparent)
                    ),
                    CircleShape
                )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 12.dp,
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 24.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(key = "header") {
                Header(state = state, glass = glass, glassBorder = glassBorder)
            }
            item(key = "status") {
                StatusSection(
                    state = state,
                    glass = glass,
                    glassBorder = glassBorder,
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
            }
            item(key = "search") {
                OutlinedTextField(
                    value = state.query,
                    onValueChange = viewModel::onQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                    placeholder = { Text(stringResource(R.string.search_apps)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = glass,
                        unfocusedContainerColor = glass.copy(alpha = glass.alpha * 0.85f),
                        focusedBorderColor = AccentBlue.copy(alpha = 0.55f),
                        unfocusedBorderColor = glassBorder,
                    ),
                )
            }

            if (state.loading) {
                item(key = "loading") {
                    GlassSurface(glass = glass, glassBorder = glassBorder) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(12.dp))
                            Text(stringResource(R.string.loading_apps), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            } else if (state.apps.isEmpty()) {
                item(key = "empty") {
                    GlassSurface(glass = glass, glassBorder = glassBorder) {
                        Text(
                            text = stringResource(R.string.no_apps_found),
                            modifier = Modifier.padding(20.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                items(state.apps, key = { it.packageName }) { app ->
                    AppRow(
                        app = app,
                        glass = glass,
                        glassBorder = glassBorder,
                        onToggle = { viewModel.toggleBlocked(app.packageName) },
                        modifier = Modifier.animateItem(
                            placementSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessMediumLow,
                            ),
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun Header(state: MainUiState, glass: Color, glassBorder: Color) {
    GlassSurface(glass = glass, glassBorder = glassBorder) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        Brush.linearGradient(listOf(AccentBlue, AccentCyan, AccentViolet)),
                        RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Rounded.Shield,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp),
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    text = stringResource(R.string.blocked_count, state.blockedCount),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            val active = state.accessibilityEnabled
            val badgeColor by animateColorAsState(
                if (active) AccentGreen.copy(alpha = 0.18f) else AccentAmber.copy(alpha = 0.18f),
                label = "badge"
            )
            val badgeTextColor by animateColorAsState(if (active) AccentGreen else AccentAmber, label = "badgeText")
            Box(
                modifier = Modifier
                    .background(badgeColor, RoundedCornerShape(999.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = stringResource(if (active) R.string.status_active else R.string.status_inactive),
                    style = MaterialTheme.typography.labelLarge,
                    color = badgeTextColor,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun StatusSection(
    state: MainUiState,
    glass: Color,
    glassBorder: Color,
    onOpenAccessibility: () -> Unit,
    onOpenBattery: () -> Unit,
) {
    GlassSurface(glass = glass, glassBorder = glassBorder) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = stringResource(R.string.protection_title),
                style = MaterialTheme.typography.titleMedium,
            )
            StatusCard(
                icon = Icons.Rounded.Security,
                title = stringResource(R.string.accessibility_title),
                description = stringResource(
                    if (state.accessibilityEnabled) R.string.accessibility_enabled else R.string.accessibility_disabled
                ),
                ok = state.accessibilityEnabled,
                actionLabel = stringResource(R.string.open_accessibility),
                onAction = onOpenAccessibility,
            )
            StatusCard(
                icon = Icons.Rounded.BatterySaver,
                title = stringResource(R.string.battery_title),
                description = stringResource(
                    if (state.batteryOptimizationIgnored) R.string.battery_ok else R.string.battery_limited
                ),
                ok = state.batteryOptimizationIgnored,
                actionLabel = stringResource(R.string.open_battery),
                onAction = onOpenBattery,
            )
            Text(
                text = stringResource(R.string.how_it_works),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun StatusCard(
    icon: ImageVector,
    title: String,
    description: String,
    ok: Boolean,
    actionLabel: String,
    onAction: () -> Unit,
) {
    val accent by animateColorAsState(if (ok) AccentGreen else AccentAmber, label = "statusAccent")
    val container by animateColorAsState(accent.copy(alpha = 0.10f), label = "statusBg")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(container)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onAction,
            )
            .padding(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.padding(top = 1.dp))
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Icon(
                    if (ok) Icons.Rounded.CheckCircle else Icons.Rounded.ErrorOutline,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(18.dp),
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(actionLabel, style = MaterialTheme.typography.labelLarge, color = accent)
                Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun AppRow(
    app: AppInfo,
    glass: Color,
    glassBorder: Color,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor by animateColorAsState(
        if (app.isBlocked) AccentRose.copy(alpha = 0.45f) else glassBorder,
        label = "rowBorder"
    )
    val scale by animateFloatAsState(
        targetValue = if (app.isBlocked) 1f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "rowScale"
    )
    val iconPainter = remember(app.packageName, app.icon) {
        app.icon?.runCatching { BitmapPainter(toBitmap().asImageBitmap()) }?.getOrNull()
    }

    GlassSurface(glass = glass, glassBorder = borderColor, modifier = modifier.scale(scale)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onToggle,
                )
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                contentAlignment = Alignment.Center,
            ) {
                if (iconPainter != null) {
                    Image(
                        painter = iconPainter,
                        contentDescription = null,
                        modifier = Modifier.size(34.dp),
                    )
                } else {
                    Icon(Icons.Rounded.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.label,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.width(8.dp))
            Switch(
                checked = app.isBlocked,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = AccentRose,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                ),
            )
        }
    }
}

@Composable
private fun GlassSurface(
    glass: Color,
    glassBorder: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, glassBorder, RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        color = glass,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        content = content,
    )
}

private fun Color.luminance(): Float = (0.299f * red + 0.587f * green + 0.114f * blue)
