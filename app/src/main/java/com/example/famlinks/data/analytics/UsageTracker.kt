package com.example.famlinks.data.analytics

import android.util.Log
import java.util.*

object UsageTracker {

    data class UploadLog(
        val userId: String,
        val mediaType: String, // "photo" or "video"
        val sizeBytes: Long,
        val timestamp: Long = System.currentTimeMillis(),
        val tier: String // "hot", "cold", "glacier"
    )

    private val uploadLogs = mutableListOf<UploadLog>()

    fun logUpload(userId: String, sizeBytes: Long, mediaType: String, tier: String) {
        val log = UploadLog(userId, mediaType, sizeBytes, tier = tier)
        uploadLogs.add(log)
        Log.d("UsageTracker", "Logged upload: $log")
    }

    fun estimateMonthlyStorageCost(userId: String): Double {
        val thisMonth = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }.timeInMillis

        val userLogs = uploadLogs.filter { it.userId == userId && it.timestamp >= thisMonth }

        val costPerGB = mapOf(
            "hot" to 0.023,
            "cold" to 0.0125,
            "glacier" to 0.004
        )

        val grouped = userLogs.groupBy { it.tier }

        return grouped.entries.sumOf { (tier, logs) ->
            val totalGB = logs.sumOf { it.sizeBytes.toDouble() } / (1024 * 1024 * 1024)
            totalGB * (costPerGB[tier] ?: 0.0)
        }
    }
    fun logView(userId: String, mediaType: String, estimatedSizeBytes: Long, tier: String) {
        val sizeMB = estimatedSizeBytes / (1024.0 * 1024.0)
        Log.d("UsageTracker", "👁️ $userId viewed $mediaType ($tier): ${String.format("%.2f", sizeMB)} MB")
    }
}

