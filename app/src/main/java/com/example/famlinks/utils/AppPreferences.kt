// File: AppPreferences.kt
package com.example.famlinks.util

import android.content.Context
import android.content.SharedPreferences
import java.util.UUID

private const val PREFS_NAME = "famlinks_prefs"
private const val KEY_GUEST_SELECTED = "guest_selected"
private const val KEY_GUEST_UUID = "guest_uuid"

// ✅ Check if user selected guest mode
fun isGuestSelected(context: Context): Boolean {
    val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getBoolean(KEY_GUEST_SELECTED, false)
}

// ✅ Mark user as having selected guest mode
fun markGuestSelected(context: Context) {
    val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().putBoolean(KEY_GUEST_SELECTED, true).apply()
}

// ✅ Get existing or generate new guest UUID
fun getOrCreateGuestId(context: Context): String {
    val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val existingId = prefs.getString(KEY_GUEST_UUID, null)

    return if (existingId != null) {
        existingId
    } else {
        val newId = "guest_${UUID.randomUUID()}"
        prefs.edit().putString(KEY_GUEST_UUID, newId).apply()
        newId
    }
}

// ✅ Get current guest UUID (or null if not set)
fun getGuestId(context: Context): String? {
    val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getString(KEY_GUEST_UUID, null)
}
