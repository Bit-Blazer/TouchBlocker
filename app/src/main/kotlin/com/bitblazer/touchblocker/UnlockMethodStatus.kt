package com.bitblazer.touchblocker

import android.content.SharedPreferences

internal class UnlockMethodStatus(
  private val sharedPreferences: SharedPreferences
) {
  private val listeners = mutableListOf<Listener>()
  private val key = "use_simple_unlock"

  interface Listener {
    fun update(useSimpleUnlock: Boolean)
  }

  fun addListener(listener: Listener) {
    listeners += listener
  }

  fun removeListener(listener: Listener) {
    listeners -= listener
  }

  fun getUseSimpleUnlock(): Boolean {
    return sharedPreferences.getBoolean(key, false)
  }

  fun setUseSimpleUnlock(useSimpleUnlock: Boolean) {
    sharedPreferences.edit().putBoolean(key, useSimpleUnlock).apply()
    for (i in listeners.indices) {
      listeners[i].update(useSimpleUnlock)
    }
  }
}
