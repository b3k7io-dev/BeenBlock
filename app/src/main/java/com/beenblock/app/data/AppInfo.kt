package com.beenblock.app.data

import androidx.compose.ui.graphics.ImageBitmap

data class AppInfo(
    val packageName: String,
    val label: String,
    val icon: ImageBitmap?,
    val isSystem: Boolean,
    val isBlocked: Boolean = false,
)
