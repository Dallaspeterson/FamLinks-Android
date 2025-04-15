// File: data/remote/s3/S3GalleryLoader.kt
package com.example.famlinks.data.remote.s3

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request
import java.time.Duration

object S3GalleryLoader {
    suspend fun listPhotoUrls(): List<String> = withContext(Dispatchers.IO) {
        val s3 = AwsS3Client.getClient() ?: return@withContext emptyList()
        val utils = AwsS3Client.getUtils() ?: return@withContext emptyList()
        val identityId = AwsS3Client.getIdentityId() ?: return@withContext emptyList()

        try {
            val prefix = "users/$identityId/"
            Log.d("S3GalleryLoader", "Looking in prefix: $prefix")

            val request = ListObjectsV2Request.builder()
                .bucket("famlinks-user-media")
                .prefix(prefix)
                .build()

            val result = s3.listObjectsV2(request)

            Log.d("S3GalleryLoader", "Found ${result.contents().size} objects in S3")
            for (obj in result.contents()) {
                Log.d("S3GalleryLoader", "🧠 S3 Key: ${obj.key()}")
            }

            result.contents()
                .filter { it.key().endsWith(".jpg", true) }
                .map { s3Object ->
                    utils.getUrl {
                        it.bucket("famlinks-user-media")
                            .key(s3Object.key())
                    }.toExternalForm()
                }

        } catch (e: Exception) {
            Log.e("S3GalleryLoader", "Failed to load gallery", e)
            emptyList()
        }
    }
}
