// File: data/remote/s3/S3GalleryLoader.kt
package com.example.famlinks.data.remote.s3

import android.content.Context
import android.util.Log
import com.amazonaws.services.s3.model.ListObjectsRequest
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest
import com.amazonaws.services.s3.model.S3ObjectSummary
import com.example.famlinks.util.UserIdProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.concurrent.TimeUnit

object S3GalleryLoader {
    suspend fun listPhotoUrls(context: Context): List<String> = withContext(Dispatchers.IO) {
        val s3 = AwsS3Client.getClient() ?: return@withContext emptyList()
        val identityId = UserIdProvider.getUserId(context)
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

            val photoUrls = objects
                .filter { it.key.endsWith(".jpg", ignoreCase = true) && !it.key.endsWith(".keep") }
                .map { obj ->
                    val expiration = Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1))
                    val requestUrl = GeneratePresignedUrlRequest(bucket, obj.key)
                        .withMethod(com.amazonaws.HttpMethod.GET)
                        .withExpiration(expiration)

                    s3.generatePresignedUrl(requestUrl).toString().also {
                        Log.d("S3GalleryLoader", "🌐 URL: $it")
                    }
                }

            Log.d("S3GalleryLoader", "✅ Loaded ${photoUrls.size} image URLs")
            photoUrls

        } catch (e: Exception) {
            Log.e("S3GalleryLoader", "❌ Failed to load gallery", e)
            emptyList()
        }
    }
}
