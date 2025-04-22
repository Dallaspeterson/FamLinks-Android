// File: ui/viewer/PhotoViewerUiState.kt
package com.example.famlinks.ui.viewer

import com.example.famlinks.data.remote.s3.S3Photo

data class PhotoViewerUiState(
    val isVisible: Boolean = false,
    val photoList: List<S3Photo> = emptyList(),
    val initialIndex: Int = 0
)
