// File: model/PendingUploadItem.kt
package com.example.famlinks.model

import java.io.File
import java.util.UUID

/**
 * Represents a photo that has been taken but not yet uploaded.
 */
data class PendingUploadItem(
    val file: File?,
    val id: String = UUID.randomUUID().toString(),
    val localPath: String,
    val timestamp: Long,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val type: UploadType = UploadType.MOMENT,
    val uploadStatus: UploadStatus = UploadStatus.QUEUED,
    val fileSizeBytes: Long? = null,
    val resolution: String? = null,
    val mediaType: String? = null
)

enum class UploadType {
    MOMENT,
    MEMORY,
    PORTAL
}

enum class UploadStatus {
    QUEUED,
    UPLOADING,
    FAILED,
    COMPLETE
}
