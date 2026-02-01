package com.bitblazer.touchblocker

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.CompoundButton
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class LauncherActivity : Activity(), FloatingViewStatus.Listener {
  private lateinit var floatingViewStatus: FloatingViewStatus
  private lateinit var keepScreenOnStatus: KeepScreenOnStatus
  private lateinit var changeScreenBrightnessStatus: ChangeScreenBrightnessStatus
  private lateinit var unlockMethodStatus: UnlockMethodStatus
  private lateinit var accessibilityPermissionRequestTracker: AccessibilityPermissionRequestTracker
  private lateinit var brandIcon: View
  private lateinit var enableButton: TextView
  private lateinit var keepScreenOnCheckBox: CompoundButton
  private lateinit var changeScreenBrightnessCheckBox: CompoundButton
  private lateinit var useSimpleUnlockCheckBox: CompoundButton
  private lateinit var assistantCheckBox: CompoundButton

  private val PREFS_NAME = "TouchBlockerPrefs"
  private val KEY_FIRST_RUN = "first_run"

  override fun onCreate(savedInstanceState: Bundle?) {
    val application = application as TouchBlockerApplication
    floatingViewStatus = application.floatingViewStatus
    keepScreenOnStatus = application.keepScreenOnStatus
    changeScreenBrightnessStatus = application.changeScreenBrightnessStatus
    unlockMethodStatus = application.unlockMethodStatus
    accessibilityPermissionRequestTracker = application.accessibilityPermissionRequestTracker

    installSplashScreen()

    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_launcher)
    
    // Handle shortcut intent
    if (intent?.getBooleanExtra("enable_blocker", false) == true) {
      if (floatingViewStatus.permissionGranted && !floatingViewStatus.added) {
        floatingViewStatus.setAdded(true)
      }
    }
    
    brandIcon = findViewById(R.id.brand_icon)
    enableButton = findViewById(R.id.enable)
    keepScreenOnCheckBox = findViewById(R.id.keep_screen_on)
    changeScreenBrightnessCheckBox = findViewById(R.id.change_screen_brightness)
    useSimpleUnlockCheckBox = findViewById(R.id.use_simple_unlock)
    assistantCheckBox = findViewById(R.id.enable_assistant)
    
    findViewById<View>(R.id.info_button).setOnClickListener {
      showTutorialDialog()
    }
    if (floatingViewStatus.added) {
      onFloatingViewAdded()
    } else if (floatingViewStatus.permissionGranted) {
      onFloatingViewRemoved()
    } else {
      onFloatingViewPermissionRevoked()
    }
    brandIcon.visibility = if (resources.configuration.orientation == ORIENTATION_PORTRAIT) {
      View.VISIBLE
    } else {
      View.GONE
    }
    keepScreenOnCheckBox.isChecked =
      keepScreenOnStatus.getKeepScreenOn()
    keepScreenOnCheckBox.setOnCheckedChangeListener { _, isChecked ->
      if (keepScreenOnCheckBox.tag != null) {
        return@setOnCheckedChangeListener
      }
      keepScreenOnStatus.setKeepScreenOn(isChecked)
    }
    changeScreenBrightnessCheckBox.isChecked =
      changeScreenBrightnessStatus.getChangeScreenBrightness()
    changeScreenBrightnessCheckBox.setOnCheckedChangeListener { _, isChecked ->
      if (changeScreenBrightnessCheckBox.tag != null) {
        return@setOnCheckedChangeListener
      }
      changeScreenBrightnessStatus.setChangeScreenBrightness(isChecked)
    }
    useSimpleUnlockCheckBox.isChecked =
      unlockMethodStatus.getUseSimpleUnlock()
    useSimpleUnlockCheckBox.setOnCheckedChangeListener { _, isChecked ->
      if (useSimpleUnlockCheckBox.tag != null) {
        return@setOnCheckedChangeListener
      }
      unlockMethodStatus.setUseSimpleUnlock(isChecked)
    }
    assistantCheckBox.isChecked = isDefaultAssistant()
    assistantCheckBox.setOnClickListener {
      startActivity(Intent(Settings.ACTION_VOICE_INPUT_SETTINGS))
      @StringRes val toastMessageResource = if (assistantCheckBox.isChecked) {
        R.string.enable_assistant_toast_disable
      } else {
        R.string.enable_assistant_toast
      }
      Toast.makeText(this, toastMessageResource, Toast.LENGTH_LONG).show()
    }
    floatingViewStatus.addListener(this)
    keepScreenOnStatus.addListener(keepScreenOnStatusListener)
    changeScreenBrightnessStatus.addListener(changeScreenBrightnessStatusListener)
    unlockMethodStatus.addListener(unlockMethodStatusListener)
    
    // Show tutorial on first run
    val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
    if (prefs.getBoolean(KEY_FIRST_RUN, true)) {
      showTutorialDialog()
      prefs.edit().putBoolean(KEY_FIRST_RUN, false).apply()
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    floatingViewStatus.removeListener(this)
    keepScreenOnStatus.removeListener(keepScreenOnStatusListener)
    changeScreenBrightnessStatus.removeListener(changeScreenBrightnessStatusListener)
    unlockMethodStatus.removeListener(unlockMethodStatusListener)
  }

  override fun onFloatingViewAdded() {
    enableButton.setText(R.string.enable_button_remove_floating)
    enableButton.setOnClickListener {
      floatingViewStatus.setAdded(false)
    }
  }

  override fun onFloatingViewRemoved() {
    enableButton.setText(R.string.enable_button_add_floating)
    enableButton.setOnClickListener {
      floatingViewStatus.setAdded(true)
    }
  }

  override fun onFloatingViewPermissionGranted() {
    onFloatingViewRemoved()
  }

  override fun onFloatingViewPermissionRevoked() {
    enableButton.setText(R.string.enable_button_accessibility_service)
    enableButton.setOnClickListener {
      showPermissionDialog()
    }
  }

  private fun showPermissionDialog() {
    val alertDialog = AlertDialog.Builder(this, R.style.DialogPermissionStyle)
      .setView(R.layout.dialog_permission)
      .show()
    alertDialog.findViewById<View>(R.id.dialog_permission_button_confirm)!!.setOnClickListener {
      alertDialog.dismiss()
      requestPermission()
    }
    alertDialog.findViewById<View>(R.id.dialog_permission_button_cancel)!!.setOnClickListener {
      alertDialog.cancel()
    }
  }

  private fun showTutorialDialog() {
    val alertDialog = AlertDialog.Builder(this, R.style.DialogPermissionStyle)
      .setView(R.layout.dialog_tutorial)
      .setCancelable(false)
      .show()
    alertDialog.findViewById<View>(R.id.tutorial_got_it_button)!!.setOnClickListener {
      alertDialog.dismiss()
    }
  }

  private fun requestPermission() {
    accessibilityPermissionRequestTracker.recordAccessibilityPermissionRequest()
    startActivity(
      accessibilityServicesSettingsIntent().addFlags(
        Intent.FLAG_ACTIVITY_NEW_TASK or
          Intent.FLAG_ACTIVITY_CLEAR_TOP or
          Intent.FLAG_ACTIVITY_SINGLE_TOP
      )
    )
  }

  override fun onToggle() {
    // No-op.
  }

  private val keepScreenOnStatusListener =
    object : KeepScreenOnStatus.Listener {
      override fun update(keepScreenOn: Boolean) {
        setKeepScreenOnCheckboxCheckedWithoutCallingListener(keepScreenOn)
      }
    }


  private val changeScreenBrightnessStatusListener =
    object : ChangeScreenBrightnessStatus.Listener {
      override fun update(changeScreenBrightness: Boolean) {
        setChangeScreenBrightnessCheckboxCheckedWithoutCallingListener(changeScreenBrightness)
      }
    }

  private val unlockMethodStatusListener =
    object : UnlockMethodStatus.Listener {
      override fun update(useSimpleUnlock: Boolean) {
        setUseSimpleUnlockCheckboxCheckedWithoutCallingListener(useSimpleUnlock)
      }
    }

  private fun setKeepScreenOnCheckboxCheckedWithoutCallingListener(checked: Boolean) {
    keepScreenOnCheckBox.tag = true
    keepScreenOnCheckBox.isChecked = checked
    keepScreenOnCheckBox.tag = null
  }

  private fun setChangeScreenBrightnessCheckboxCheckedWithoutCallingListener(checked: Boolean) {
    changeScreenBrightnessCheckBox.tag = true
    changeScreenBrightnessCheckBox.isChecked = checked
    changeScreenBrightnessCheckBox.tag = null
  }

  private fun setUseSimpleUnlockCheckboxCheckedWithoutCallingListener(checked: Boolean) {
    useSimpleUnlockCheckBox.tag = true
    useSimpleUnlockCheckBox.isChecked = checked
    useSimpleUnlockCheckBox.tag = null
  }

  override fun onResume() {
    super.onResume()
    // I do not know of a broadcast to know exactly when the default assistant app changed,
    // so I will check in onResume.
    assistantCheckBox.isChecked = isDefaultAssistant()
  }

  private fun isDefaultAssistant(): Boolean {
    // This only takes a millisecond or two on my Pixel 6.
    val assistant = Settings.Secure.getString(contentResolver, "assistant")
    return assistant == "$packageName/${NoDisplayActivity::class.qualifiedName}"
  }
}
