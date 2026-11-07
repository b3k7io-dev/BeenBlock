package com.beenblock.app.service

import android.accessibilityservice.AccessibilityService
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import com.beenblock.app.data.BlockedAppsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicReference

class AppBlockerAccessibilityService : AccessibilityService() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Default + serviceJob)
    private val blockedRef = AtomicReference<Set<String>>(emptySet())
    private val lastBlockedAction = AtomicReference<Pair<String, Long>?>(null)

    override fun onServiceConnected() {
        super.onServiceConnected()
        val repo = BlockedAppsRepository(applicationContext)
        serviceScope.launch {
            repo.blockedPackages.collectLatest { blocked ->
                blockedRef.set(blocked)
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val type = event.eventType
        if (type != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            type != AccessibilityEvent.TYPE_WINDOWS_CHANGED
        ) return

        val pkg = event.packageName?.toString() ?: return
        if (pkg == packageName) return
        if (!blockedRef.get().contains(pkg)) return

        val now = SystemClock.uptimeMillis()
        val last = lastBlockedAction.get()
        if (last != null && last.first == pkg && now - last.second < 900L) return
        lastBlockedAction.set(pkg to now)

        performGlobalAction(GLOBAL_ACTION_HOME)
    }

    override fun onInterrupt() {
        // No-op: service will continue receiving events after transient interruptions.
    }

    override fun onDestroy() {
        serviceJob.cancel()
        super.onDestroy()
    }
}
