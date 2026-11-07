package com.beenblock.app.ui

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.beenblock.app.ui.theme.AccentAmber
import com.beenblock.app.ui.theme.AccentBlue
import com.beenblock.app.ui.theme.AccentCyan
import com.beenblock.app.ui.theme.AccentGreen
import com.beenblock.app.ui.theme.AccentRose
import com.beenblock.app.ui.theme.AccentViolet

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

    val dark = isDark()
    val glass = if (dark) Color(0xCC151E36) else Color(0xE6FFFFFF)
    val glassBorder = if (dark) Color(0x33FFFFFF) else Color(0x260B1020)
    val bgBrush = if (dark) {
        Brush.verticalGradient(listOf(Color(0xFF090E1A), Color(0xFF101937), Color(0xFF0B1226)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFF8FBFF), Color(0xFFEEF4FF), Color(0xFFF5F8FF)))
    }

    val setupComplete = state.accessibilityEnabled && state.batteryOptimizationIgnored

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgBrush)
    ) {
        LiquidBackground(dark = dark)

        if (!setupComplete) {
            SetupGateScreen(
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
        } else {
            AppListScreen(
                state = state,
                glass = glass,
                glassBorder = glassBorder,
                onQueryChange = viewModel::onQueryChange,
                onToggleBlocked = viewModel::toggleBlocked,
            )
        }
    }
}

@Composable
private fun LiquidBackground(dark: Boolean) {
    Box(
        modifier = Modifier
            .padding(top = 24.dp, start = 170.dp)
            .size(280.dp)
            .background(
                Brush.radialGradient(
                    listOf(AccentBlue.copy(alpha = if (dark) 0.20f else 0.16f), Color.Transparent)
                ),
                CircleShape
            )
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 140.dp),
        contentAlignment = Alignment.BottomStart
    ) {
        Box(
            modifier = Modifier
                .size(240.dp)
                .background(
                    Brush.radialGradient(
                        listOf(AccentViolet.copy(alpha = if (dark) 0.16f else 0.12f), Color.Transparent)
                    ),
                    CircleShape
                )
        )
    }
}

@Composable
private fun SetupGateScreen(
    state: MainUiState,
    glass: Color,
    glassBorder: Color,
    onOpenAccessibility: () -> Unit,
    onOpenBattery: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 16.dp,
            bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            GlassSurface(glass = glass, glassBorder = glassBorder) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .background(
                                Brush.linearGradient(listOf(AccentBlue, AccentCyan, AccentViolet)),
                                RoundedCornerShape(18.dp)
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Rounded.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = stringResource(R.string.setup_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.setup_subtitle),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        item {
            SetupStepCard(
                index = 1,
                icon = Icons.Rounded.Security,
                title = stringResource(R.string.accessibility_title),
                description = stringResource(
                    if (state.accessibilityEnabled) R.string.accessibility_enabled else R.string.accessibility_disabled
                ),
                done = state.accessibilityEnabled,
                actionLabel = stringResource(R.string.open_accessibility),
                glass = glass,
                glassBorder = glassBorder,
                onAction = onOpenAccessibility,
            )
        }
        item {
            SetupStepCard(
                index = 2,
                icon = Icons.Rounded.BatterySaver,
                title = stringResource(R.string.battery_title),
                description = stringResource(
                    if (state.batteryOptimizationIgnored) R.string.battery_ok else R.string.battery_limited
                ),
                done = state.batteryOptimizationIgnored,
                actionLabel = stringResource(R.string.open_battery),
                glass = glass,
                glassBorder = glassBorder,
                onAction = onOpenBattery,
            )
        }
        item {
            GlassSurface(glass = glass, glassBorder = glassBorder) {
                Text(
                    text = stringResource(R.string.setup_footer),
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
    glass: Color,
    glassBorder: Color,
    onAction: () -> Unit,
) {
    val accent by animateColorAsState(if (done) AccentGreen else AccentAmber, label = "setupAccent")
    val border by animateColorAsState(if (done) AccentGreen.copy(alpha = 0.45f) else glassBorder, label = "setupBorder")
    GlassSurface(glass = glass, glassBorder = border) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(accent.copy(alpha = 0.16f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("$index", color = accent, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(10.dp))
                Icon(icon, contentDescription = null, tint = accent)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    if (done) Icons.Rounded.CheckCircle else Icons.Rounded.ErrorOutline,
                    contentDescription = null,
                    tint = accent,
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onAction,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accent.copy(alpha = if (done) 0.18f else 1f),
                    contentColor = if (done) accent else Color.White,
                ),
            ) {
                Text(if (done) stringResource(R.string.check_again) else actionLabel)
                Spacer(Modifier.width(6.dp))
                Icon(Icons.Rounded.ChevronRight, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun AppListScreen(
    state: MainUiState,
    glass: Color,
    glassBorder: Color,
    onQueryChange: (String) -> Unit,
    onToggleBlocked: (String) -> Unit,
) {
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
                    focusedContainerColor = glass,
                    unfocusedContainerColor = glass,
                    focusedBorderColor = AccentBlue.copy(alpha = 0.65f),
                    unfocusedBorderColor = glassBorder,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    cursorColor = AccentBlue,
                    focusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
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
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp, color = AccentBlue)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            stringResource(R.string.loading_apps),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
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
            items(state.apps, key = { it.packageName }, contentType = { "app" }) { app ->
                AppRow(
                    app = app,
                    glass = glass,
                    glassBorder = glassBorder,
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
                Icon(Icons.Rounded.Shield, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.blocked_count, state.blockedCount),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Box(
                modifier = Modifier
                    .background(AccentGreen.copy(alpha = 0.16f), RoundedCornerShape(999.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = stringResource(R.string.status_active),
                    style = MaterialTheme.typography.labelLarge,
                    color = AccentGreen,
                    fontWeight = FontWeight.SemiBold,
                )
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
        if (app.isBlocked) AccentRose.copy(alpha = 0.55f) else glassBorder,
        label = "rowBorder"
    )
    val iconPainter = remember(app.packageName, app.icon) { app.icon?.let { BitmapPainter(it) } }

    GlassSurface(glass = glass, glassBorder = borderColor, modifier = modifier) {
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
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.60f)),
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
                    color = MaterialTheme.colorScheme.onSurface,
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
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f),
                    uncheckedThumbColor = Color.White,
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
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        content = content,
    )
}

@Composable
private fun isDark(): Boolean = MaterialTheme.colorScheme.background.luminance() < 0.5f

private fun Color.luminance(): Float = (0.299f * red + 0.587f * green + 0.114f * blue)
