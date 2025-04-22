// File: data/remote/s3/S3Photo.kt
package com.example.famlinks.data.remote.s3

data class S3Photo(
    val key: String,              // Full S3 key: users/{id}/cold/photo.jpg
    val url: String,              // Presigned URL
    val tier: String = "cold",    // "preview", "cold", or "original"
    val mediaType: String = "photo", // "photo" or "video"
    val resolution: String? = null,  // Optional: e.g., 1080x720
    val sizeBytes: Long? = null      // Optional: for cost/logging previews
)
