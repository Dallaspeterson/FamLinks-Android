package com.example.famlinks.util

import android.content.Context
import java.util.UUID

object UserIdProvider {
    fun getUserId(context: Context): String {
        val sharedPrefs = context.getSharedPreferences("FamLinksPrefs", Context.MODE_PRIVATE)
        val cloudId = sharedPrefs.getString("cloudUserId", null)
        val localId = sharedPrefs.getString("localUserId", null)

        return cloudId ?: localId ?: run {
            val newId = "guest_${UUID.randomUUID()}"
            sharedPrefs.edit().putString("localUserId", newId).apply()
            newId
        }
    }

    fun saveCloudUserId(context: Context, identityId: String) {
        val sharedPrefs = context.getSharedPreferences("FamLinksPrefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().putString("cloudUserId", identityId).apply()
    }
}
