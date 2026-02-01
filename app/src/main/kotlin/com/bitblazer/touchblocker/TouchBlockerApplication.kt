package com.bitblazer.touchblocker

import android.app.Application

class TouchBlockerApplication : Application() {
  internal lateinit var floatingViewStatus: FloatingViewStatus
  internal lateinit var keepScreenOnStatus: KeepScreenOnStatus
  internal lateinit var changeScreenBrightnessStatus: ChangeScreenBrightnessStatus
  internal lateinit var unlockMethodStatus: UnlockMethodStatus
  internal lateinit var accessibilityPermissionRequestTracker: AccessibilityPermissionRequestTracker

  override fun onCreate() {
    super.onCreate()
    floatingViewStatus = FloatingViewStatus(isAccessibilityServiceEnabled(
      this,
      TouchBlockerAccessibilityService::class.java
    ))
    keepScreenOnStatus = KeepScreenOnStatus(
      getSharedPreferences(
        "keep_screen_on_status",
        MODE_PRIVATE
      )
    )
    changeScreenBrightnessStatus = ChangeScreenBrightnessStatus(
      getSharedPreferences(
        "change_screen_brightness_status",
        MODE_PRIVATE
      )
    )
    unlockMethodStatus = UnlockMethodStatus(
      getSharedPreferences(
        "unlock_method_status",
        MODE_PRIVATE
      )
    )
    accessibilityPermissionRequestTracker = AccessibilityPermissionRequestTracker()
  }
}
