// File: data/local/GuestManager.kt
package com.example.famlinks.data.local

import android.content.Context
import android.content.SharedPreferences
import java.util.UUID

class GuestManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("famlinks_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_GUEST_UUID = "guest_id"
        private const val KEY_IS_GUEST = "is_guest"
    }

    fun isGuest(): Boolean {
        return prefs.getBoolean(KEY_IS_GUEST, false)
    }

    fun getGuestUUID(): String? {
        return prefs.getString(KEY_GUEST_UUID, null)
    }

    fun generateAndSaveGuestUUID(): String {
        val uuid = UUID.randomUUID().toString()
        prefs.edit()
            .putString(KEY_GUEST_UUID, uuid)
            .putBoolean(KEY_IS_GUEST, true)
            .apply()
        return uuid
    }

    fun clearGuestData() {
        prefs.edit()
            .remove(KEY_GUEST_UUID)
            .remove(KEY_IS_GUEST)
            .apply()
    }
}

