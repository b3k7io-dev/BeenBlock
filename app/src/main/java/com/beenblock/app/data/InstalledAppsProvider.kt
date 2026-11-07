package com.beenblock.app.data

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class InstalledAppsProvider(private val context: Context) {
    private val pm: PackageManager = context.packageManager
    private val selfPackage: String = context.packageName

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
                pm.getApplicationIcon(appInfo)
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
}
