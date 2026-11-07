package com.beenblock.app.data

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

class InstalledAppsProvider(private val context: Context) {
    private val pm: PackageManager = context.packageManager
    private val selfPackage: String = context.packageName
    private val iconSizePx: Int = (48 * context.resources.displayMetrics.density).roundToInt().coerceAtLeast(72)

    suspend fun loadLaunchableApps(includeSystem: Boolean = false): List<AppInfo> = withContext(Dispatchers.IO) {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val activities = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(intent, 0)
        }

        val seen = LinkedHashSet<String>()
        val result = mutableListOf<AppInfo>()
        for (ri in activities) {
            val pkg = ri.activityInfo?.packageName ?: continue
            if (pkg == selfPackage) continue
            if (!seen.add(pkg)) continue
            val appInfo = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    pm.getApplicationInfo(pkg, PackageManager.ApplicationInfoFlags.of(0))
                } else {
                    @Suppress("DEPRECATION")
                    pm.getApplicationInfo(pkg, 0)
                }
            } catch (_: PackageManager.NameNotFoundException) {
                continue
            }
            val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0 ||
                (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
            if (!includeSystem && isSystem) continue
            val label = pm.getApplicationLabel(appInfo)?.toString()?.ifBlank { pkg } ?: pkg
            val icon = try {
                pm.getApplicationIcon(appInfo).toImageBitmap(iconSizePx)
            } catch (_: Exception) {
                null
            }
            result += AppInfo(
                packageName = pkg,
                label = label,
                icon = icon,
                isSystem = isSystem,
            )
        }
        result.sortedBy { it.label.lowercase() }
    }

    private fun Drawable.toImageBitmap(sizePx: Int): ImageBitmap {
        if (this is BitmapDrawable && bitmap != null) {
            val bmp = bitmap
            val scaled = if (bmp.width == sizePx && bmp.height == sizePx) bmp
            else Bitmap.createScaledBitmap(bmp, sizePx, sizePx, true)
            return scaled.asImageBitmap()
        }
        val w = if (intrinsicWidth > 0) intrinsicWidth else sizePx
        val h = if (intrinsicHeight > 0) intrinsicHeight else sizePx
        val bitmap = Bitmap.createBitmap(w.coerceAtLeast(1), h.coerceAtLeast(1), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        setBounds(0, 0, canvas.width, canvas.height)
        draw(canvas)
        val scaled = if (bitmap.width == sizePx && bitmap.height == sizePx) bitmap
        else Bitmap.createScaledBitmap(bitmap, sizePx, sizePx, true)
        return scaled.asImageBitmap()
    }
}
