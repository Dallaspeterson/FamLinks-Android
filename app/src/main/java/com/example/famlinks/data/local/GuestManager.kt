// File: data/local/GuestManager.kt
package com.example.famlinks.data.local

import android.content.Context
import android.content.SharedPreferences
import java.util.UUID

class GuestManager(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "famlinks_prefs"
        private const val KEY_GUEST_SELECTED = "guest_selected"
        private const val KEY_GUEST_UUID = "guest_uuid"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isGuest(): Boolean {
        return prefs.getBoolean(KEY_GUEST_SELECTED, false) && prefs.getString(KEY_GUEST_UUID, null) != null
    }

    fun getGuestUUID(): String? {
        return prefs.getString(KEY_GUEST_UUID, null)
    }

    fun generateAndSaveGuestUUID(): String {
        val newId = "guest_${UUID.randomUUID()}"
        prefs.edit().putString(KEY_GUEST_UUID, newId).apply()
        prefs.edit().putBoolean(KEY_GUEST_SELECTED, true).apply()
        return newId
    }

    // ✅ Add this method
    fun getOrCreateGuestUUID(): String {
        return getGuestUUID() ?: generateAndSaveGuestUUID()
    }
}

