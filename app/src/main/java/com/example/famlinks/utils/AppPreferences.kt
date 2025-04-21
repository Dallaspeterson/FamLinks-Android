package com.example.famlinks.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import java.util.UUID

private const val PREFS_NAME = "famlinks_prefs"
private const val KEY_GUEST_SELECTED = "guest_selected"
private const val KEY_GUEST_UUID = "guest_uuid"
private const val KEY_WIFI_ONLY_UPLOADS = "wifi_only_uploads"
private const val KEY_ALLOW_CELLULAR_UPLOAD = "allow_cellular_upload"

object AppPreferences {

    fun isGuestSelected(context: Context): Boolean {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_GUEST_SELECTED, false)
    }

    fun markGuestSelected(context: Context) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_GUEST_SELECTED, true).apply()
    }

    fun getOrCreateGuestId(context: Context): String {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val existingId = prefs.getString(KEY_GUEST_UUID, null)
        return if (existingId != null) {
            Log.i("AppPreferences", "✅ Using existing guest UUID: $existingId")
            existingId
        } else {
            val newId = "guest_${UUID.randomUUID()}"
            prefs.edit().putString(KEY_GUEST_UUID, newId).apply()
            Log.w("AppPreferences", "⚠️ Created NEW guest UUID: $newId")
            newId
        }
    }

    fun getGuestId(context: Context): String? {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_GUEST_UUID, null)
    }

    // ✅ NEW: Wi-Fi only upload preference
    fun isWifiOnlyUploadsEnabled(context: Context): Boolean {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_WIFI_ONLY_UPLOADS, true) // default to true
    }

    fun setWifiOnlyUploadsEnabled(context: Context, enabled: Boolean) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_WIFI_ONLY_UPLOADS, enabled).apply()
        Log.i("AppPreferences", "📶 Wi-Fi only uploads set to: $enabled")
    }
    fun isDataAllowed(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_ALLOW_CELLULAR_UPLOAD, false)
    }

    fun setAllowCellularUpload(context: Context, allowed: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_ALLOW_CELLULAR_UPLOAD, allowed).apply()
    }
}

