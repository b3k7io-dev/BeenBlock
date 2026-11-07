# BeenBlock ProGuard rules
# Keep accessibility service and app entry points discoverable by the system/framework.
-keep class com.beenblock.app.MainActivity
-keep class com.beenblock.app.service.AppBlockerAccessibilityService
-dontwarn coil.**
