// File: data/remote/s3/S3GalleryLoader.kt
package com.example.famlinks.data.remote.s3

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request
import com.example.famlinks.data.remote.s3.AwsS3Client

object S3GalleryLoader {
    suspend fun listPhotoUrls(): List<String> = withContext(Dispatchers.IO) {
        val s3 = AwsS3Client.getClient() ?: return@withContext emptyList()
        val utils = AwsS3Client.getUtils() ?: return@withContext emptyList()
        val identityId = AwsS3Client.getIdentityId() ?: return@withContext emptyList()

        try {
            val request = ListObjectsV2Request.builder()
                .bucket("famlinks-user-media")
                .prefix("users/$identityId/")
                .build()

            val result = s3.listObjectsV2(request)

            result.contents()
                .filter { it.key().endsWith(".jpg", true) }
                .map {
                    utils.getUrl { b ->
                        b.bucket("famlinks-user-media").key(it.key())
                    }.toExternalForm()
                }
        } catch (e: Exception) {
            Log.e("S3GalleryLoader", "Failed to load gallery", e)
            emptyList()
        }
    }
}
