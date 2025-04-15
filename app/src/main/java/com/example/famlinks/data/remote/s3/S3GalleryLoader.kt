// File: data/remote/s3/S3GalleryLoader.kt
package com.example.famlinks.data.remote.s3

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.concurrent.TimeUnit
import com.amazonaws.services.s3.model.ListObjectsRequest
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest
import com.amazonaws.services.s3.model.S3ObjectSummary

object S3GalleryLoader {
    suspend fun listPhotoUrls(): List<String> = withContext(Dispatchers.IO) {
        val s3 = AwsS3Client.getClient() ?: return@withContext emptyList()
        val identityId = AwsS3Client.getIdentityId() ?: return@withContext emptyList()
        val bucket = AwsS3Client.getBucketName()

        Log.d("S3GalleryLoader", "🪣 Using bucket: $bucket")
        Log.d("S3GalleryLoader", "🧍 Using identityId: $identityId")

        try {
            val prefix = "users/$identityId/"
            Log.d("S3GalleryLoader", "📁 Looking in prefix: $prefix")

            val request = ListObjectsRequest()
                .withBucketName(bucket)
                .withPrefix(prefix)

            val objects = mutableListOf<S3ObjectSummary>()
            var listing = s3.listObjects(request)

            do {
                objects.addAll(listing.objectSummaries)
                listing = if (listing.isTruncated) {
                    s3.listNextBatchOfObjects(listing)
                } else {
                    null
                }
            } while (listing != null)

            Log.d("S3GalleryLoader", "📦 Found ${objects.size} objects:")
            objects.forEach { obj ->
                Log.d("S3GalleryLoader", "🧠 ${obj.key}")
            }

            val photoUrls = objects
                .filter { summary ->
                    val valid = summary.key.endsWith(".jpg", ignoreCase = true)
                            && !summary.key.endsWith(".keep")
                    Log.d("S3GalleryLoader", "🔎 Checking key: ${summary.key} -> valid: $valid")
                    valid
                }
                .map { obj ->
                    val expiration = Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1))
                    val presignedRequest = GeneratePresignedUrlRequest(bucket, obj.key)
                        .withMethod(com.amazonaws.HttpMethod.GET)
                        .withExpiration(expiration)

                    s3.generatePresignedUrl(presignedRequest).toString().also {
                        Log.d("S3GalleryLoader", "🌐 Generated URL: $it")
                    }
                }

            Log.d("S3GalleryLoader", "✅ Returning ${photoUrls.size} image URLs")
            photoUrls

        } catch (e: Exception) {
            Log.e("S3GalleryLoader", "❌ Failed to load gallery", e)
            emptyList()
        }
    }
}


