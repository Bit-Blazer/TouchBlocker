package com.bitblazer.touchblocker

import android.app.Activity
import android.content.Intent
import android.os.Bundle

class NoDisplayActivity : Activity() {
  private lateinit var floatingViewStatus: FloatingViewStatus

  override fun onCreate(savedInstanceState: Bundle?) {
    val application = application as TouchBlockerApplication
    floatingViewStatus = application.floatingViewStatus
    super.onCreate(savedInstanceState)
    
    // Handle shortcut intent
    val enableBlocker = intent?.getBooleanExtra("enable_blocker", false) == true
    
    if (floatingViewStatus.permissionGranted) {
      if (enableBlocker && !floatingViewStatus.added) {
        floatingViewStatus.setAdded(true)
      } else {
        floatingViewStatus.toggle()
      }
    } else {
      startActivity(
        accessibilityServicesSettingsIntent().addFlags(
          Intent.FLAG_ACTIVITY_NEW_TASK or
            Intent.FLAG_ACTIVITY_CLEAR_TOP or
            Intent.FLAG_ACTIVITY_SINGLE_TOP
        )
      )
    }
    finish()
  }
}
