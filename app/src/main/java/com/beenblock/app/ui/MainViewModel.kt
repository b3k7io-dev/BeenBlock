package com.beenblock.app.ui

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.os.PowerManager
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.beenblock.app.data.AppInfo
import com.beenblock.app.data.BlockedAppsRepository
import com.beenblock.app.data.InstalledAppsProvider
import com.beenblock.app.service.AppBlockerAccessibilityService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MainUiState(
    val loading: Boolean = true,
    val query: String = "",
    val apps: List<AppInfo> = emptyList(),
    val blockedCount: Int = 0,
    val accessibilityEnabled: Boolean = false,
    val batteryOptimizationIgnored: Boolean = false,
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = BlockedAppsRepository(application)
    private val provider = InstalledAppsProvider(application)

    private val loading = MutableStateFlow(true)
    private val query = MutableStateFlow("")
    private val rawApps = MutableStateFlow<List<AppInfo>>(emptyList())
    private val accessibilityEnabled = MutableStateFlow(false)
    private val batteryIgnored = MutableStateFlow(false)

    val uiState: StateFlow<MainUiState> = combine(
        combine(loading, query, rawApps) { isLoading, q, apps -> Triple(isLoading, q, apps) },
        combine(repo.blockedPackages, accessibilityEnabled, batteryIgnored) { blocked, acc, batt -> Triple(blocked, acc, batt) },
    ) { left, right ->
        val (isLoading, q, apps) = left
        val (blocked, acc, batt) = right

        val merged = apps.map { it.copy(isBlocked = blocked.contains(it.packageName)) }
        val filtered = if (q.isBlank()) merged else {
            val needle = q.trim().lowercase()
            merged.filter {
                it.label.lowercase().contains(needle) || it.packageName.lowercase().contains(needle)
            }
        }
        MainUiState(
            loading = isLoading,
            query = q,
            apps = filtered,
            blockedCount = blocked.size,
            accessibilityEnabled = acc,
            batteryOptimizationIgnored = batt,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MainUiState())

    init {
        refreshSystemStatus()
        viewModelScope.launch {
            runCatching { provider.loadLaunchableApps(includeSystem = false) }
                .onSuccess { rawApps.value = it }
            loading.value = false
        }
    }

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun toggleBlocked(packageName: String) {
        viewModelScope.launch {
            repo.toggle(packageName)
        }
    }

    fun refreshSystemStatus() {
        val context = getApplication<Application>()
        accessibilityEnabled.value = isAccessibilityEnabled(context)
        batteryIgnored.value = isIgnoringBatteryOptimizations(context)
    }

    private fun isAccessibilityEnabled(context: Context): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager ?: return false
        val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        val expected = ComponentName(context, AppBlockerAccessibilityService::class.java)
        val flat = expected.flattenToString()
        val short = expected.flattenToShortString()
        if (enabledServices.any { info ->
                val id = info.id ?: return@any false
                id == flat || id == short || id.contains(context.packageName)
            }) return true

        val setting = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
            ?: return false
        return setting.split(':').any { it.equals(flat, true) || it.equals(short, true) || it.contains(AppBlockerAccessibilityService::class.java.simpleName) }
    }

    private fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return false
        return pm.isIgnoringBatteryOptimizations(context.packageName)
    }
}
