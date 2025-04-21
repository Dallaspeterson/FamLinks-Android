// File: data/upload/UploadManager.kt
package com.example.famlinks.data.upload

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.example.famlinks.data.remote.metadata.DynamoMetadataItem
import com.example.famlinks.data.remote.metadata.MetadataUploader
import com.example.famlinks.data.remote.s3.S3Uploader
import com.example.famlinks.model.UploadStatus
import com.example.famlinks.viewmodel.PendingUploadsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File

object UploadManager {
    private var uploadJob: Job? = null

    fun startUploading(
        context: Context,
        viewModel: PendingUploadsViewModel,
        allowCellular: Boolean,
        galleryViewModel: com.example.famlinks.viewmodel.GalleryViewModel
    )
    {
        if (uploadJob?.isActive == true) return // Already uploading

        uploadJob = CoroutineScope(Dispatchers.IO).launch {
            val pendingItems = viewModel.pendingUploads.value.filter {
                it.uploadStatus == UploadStatus.QUEUED
            }

            for (item in pendingItems) {
                val canUpload = isNetworkAvailable(context, allowCellular)
                if (!canUpload) break

                val file = item.file
                if (file == null || !file.exists()) {
                    Log.e("UploadManager", "❌ Missing file for item ${item.id}")
                    viewModel.markAsFailed(item.id)
                    continue
                }

                viewModel.markAsUploading(item.id)

                val success = S3Uploader.uploadPhoto(context, file)
                if (success) {
                    val identityId = com.example.famlinks.util.UserIdProvider.getUserId(context)
                    val metadataItem = DynamoMetadataItem().apply {
                        this.identityId = identityId
                        this.photoKey = "users/$identityId/${file.name}"
                        this.timestamp = item.timestamp
                        this.latitude = item.latitude
                        this.longitude = item.longitude
                    }

                    MetadataUploader.uploadMetadata(context, metadataItem)

                    viewModel.markAsUploaded(item.id)
                    viewModel.removeItem(item.id, context)

                    galleryViewModel.markAsStale() // 👈 NEW: allow refresh

                    Log.i("UploadManager", "✅ Uploaded to S3: ${file.name}")
                } else {
                    viewModel.markAsFailed(item.id)
                    Log.e("UploadManager", "❌ Upload failed: ${file.name}")
                }
            }
        }
    }

    private fun isNetworkAvailable(context: Context, allowCellular: Boolean): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return false

        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            allowCellular && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            else -> false
        }
    }
}


