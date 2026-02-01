package com.bitblazer.touchblocker

import android.content.Intent
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

class TouchBlockerTileService : TileService(), FloatingViewStatus.Listener {
  private lateinit var floatingViewStatus: FloatingViewStatus

  override fun onCreate() {
    super.onCreate()
    val application = application as TouchBlockerApplication
    floatingViewStatus = application.floatingViewStatus
  }

  override fun onStartListening() {
    super.onStartListening()
    floatingViewStatus.addListener(this)
    updateTile()
  }

  override fun onStopListening() {
    super.onStopListening()
    floatingViewStatus.removeListener(this)
  }

  override fun onClick() {
    super.onClick()
    
    if (!floatingViewStatus.permissionGranted) {
      // Open the app to request permission
      val intent = Intent(this, LauncherActivity::class.java).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      startActivityAndCollapse(intent)
      return
    }

    // Toggle the overlay visibility (mimics Enable/Disable button)
    floatingViewStatus.setAdded(!floatingViewStatus.added)
  }

  override fun onFloatingViewAdded() {
    updateTile()
  }

  override fun onFloatingViewRemoved() {
    updateTile()
  }

  override fun onFloatingViewPermissionGranted() {
    updateTile()
  }

  override fun onFloatingViewPermissionRevoked() {
    updateTile()
  }

  override fun onToggle() {
    updateTile()
  }

  private fun updateTile() {
    qsTile?.apply {
      state = if (floatingViewStatus.added) {
        Tile.STATE_ACTIVE
      } else {
        Tile.STATE_INACTIVE
      }
      label = getString(R.string.app_name)
      subtitle = if (floatingViewStatus.added) {
        "Blocking"
      } else {
        "Inactive"
      }
      updateTile()
    }
  }
}
