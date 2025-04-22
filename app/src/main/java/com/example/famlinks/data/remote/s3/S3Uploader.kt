// File: data/remote/s3/S3Uploader.kt
package com.example.famlinks.data.remote.s3

import android.content.Context
import android.util.Log
import com.amazonaws.services.s3.model.PutObjectRequest
import com.amazonaws.services.s3.model.StorageClass
import com.example.famlinks.data.analytics.UsageTracker
import com.example.famlinks.data.remote.metadata.DynamoMetadataItem
import com.example.famlinks.data.remote.metadata.MetadataUploader
import com.example.famlinks.util.UserIdProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

object S3Uploader {
    private const val BUCKET_NAME = "famlinks-user-media"

    fun uploadPhoto(context: Context, file: File): Boolean {
        return try {
            val s3 = AwsS3Client.getClient() ?: run {
                Log.e("S3Uploader", "❌ S3 client not initialized")
                return false
            }

            val identityId = UserIdProvider.getUserId(context)
            val key = "users/$identityId/${file.name}"

            Log.i("S3Uploader", "📤 Uploading to bucket: $BUCKET_NAME, Key: $key")
            val putRequest = PutObjectRequest(BUCKET_NAME, key, file)
            s3.putObject(putRequest)

            Log.i("S3Uploader", "✅ Upload successful for key: $key")
            true
        } catch (e: Exception) {
            Log.e("S3Uploader", "❌ Upload failed", e)
            false
        }
    }

    fun uploadMultiTierPhoto(
        context: Context,
        originalFile: File,
        coldCompressedFile: File,
        thumbnailFile: File,
        timestamp: Long,
        latitude: Double?,
        longitude: Double?
    ): Boolean {
        return try {
            val s3 = AwsS3Client.getClient() ?: return false
            val identityId = UserIdProvider.getUserId(context)

            val originalKey = "users/$identityId/original/${originalFile.name}"
            val coldKey = "users/$identityId/cold/${coldCompressedFile.name}"
            val previewKey = "users/$identityId/preview/${thumbnailFile.name}"

            // Upload original → Glacier
            val originalRequest = PutObjectRequest(BUCKET_NAME, originalKey, originalFile)
                .withStorageClass(StorageClass.Glacier)
            s3.putObject(originalRequest)
            Log.i("S3Uploader", "✅ Uploaded original to Glacier: $originalKey")

            // Upload 1080p → Cold (STANDARD_IA)
            val coldRequest = PutObjectRequest(BUCKET_NAME, coldKey, coldCompressedFile)
                .withStorageClass(StorageClass.StandardInfrequentAccess)
            s3.putObject(coldRequest)
            Log.i("S3Uploader", "✅ Uploaded cold version: $coldKey")

            // Upload thumbnail → Cold (STANDARD_IA)
            val previewRequest = PutObjectRequest(BUCKET_NAME, previewKey, thumbnailFile)
                .withStorageClass(StorageClass.StandardInfrequentAccess)
            s3.putObject(previewRequest)
            Log.i("S3Uploader", "✅ Uploaded thumbnail: $previewKey")

            // Log usage for cost tracking (based on 1080p version)
            UsageTracker.logUpload(
                userId = identityId,
                sizeBytes = coldCompressedFile.length(),
                mediaType = "photo",
                tier = "cold"
            )

            // Save metadata for cold version only (used in viewer)
            val metadataItem = DynamoMetadataItem().apply {
                this.identityId = identityId
                this.photoKey = coldKey
                this.timestamp = timestamp
                this.latitude = latitude
                this.longitude = longitude
                this.fileSizeBytes = coldCompressedFile.length()
                this.resolution = com.example.famlinks.utils.getImageResolution(coldCompressedFile)
                this.mediaType = "photo"
            }

            CoroutineScope(Dispatchers.IO).launch {
                MetadataUploader.uploadMetadata(context, metadataItem)
            }

            true
        } catch (e: Exception) {
            Log.e("S3Uploader", "❌ Multi-tier upload failed", e)
            false
        }
    }
}

