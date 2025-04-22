// File: data/remote/s3/S3GalleryLoader.kt
package com.example.famlinks.data.remote.s3

import android.content.Context
import android.util.Log
import com.amazonaws.services.s3.model.ListObjectsV2Request
import com.amazonaws.services.s3.model.S3ObjectSummary
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest
import com.example.famlinks.util.UserIdProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.concurrent.TimeUnit

data class PageResult(
    val photos: List<S3Photo>,
    val nextToken: String? // null if you're done
)

object S3GalleryLoader {

    suspend fun loadPhotoPage(
        context: Context,
        maxKeys: Int = 50,
        continuationToken: String? = null
    ): PageResult = withContext(Dispatchers.IO) {
        val s3 = AwsS3Client.getClient() ?: return@withContext PageResult(emptyList(), null)
        val identityId = UserIdProvider.getUserId(context)
        val bucket = AwsS3Client.getBucketName()
        val prefix = "users/$identityId/preview/"

        Log.d("S3GalleryLoader", "📦 Loading preview photo page: max=$maxKeys, token=$continuationToken")

        try {
            val request = ListObjectsV2Request()
                .withBucketName(bucket)
                .withPrefix(prefix)
                .withMaxKeys(maxKeys)
                .withContinuationToken(continuationToken)

            val result = s3.listObjectsV2(request)

            val photos = result.objectSummaries
                .filter { it.key.endsWith(".jpg", ignoreCase = true) && !it.key.endsWith(".keep") }
                .map { obj ->
                    val expiration = Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1))
                    val presignedUrl = s3.generatePresignedUrl(
                        GeneratePresignedUrlRequest(bucket, obj.key)
                            .withMethod(com.amazonaws.HttpMethod.GET)
                            .withExpiration(expiration)
                    )
                    val sizeBytes = obj.size.toLong()

                    S3Photo(
                        key = obj.key,
                        url = presignedUrl.toString(),
                        tier = "preview",
                        mediaType = "photo",
                        sizeBytes = sizeBytes
                    ).also {
                        Log.d("S3GalleryLoader", "🖼️ ${it.key} (${String.format("%.2f", sizeBytes / 1024.0)} KB)")
                    }
                }

            Log.d("S3GalleryLoader", "✅ Loaded page: ${photos.size} preview images")

            return@withContext PageResult(
                photos = photos.reversed(), // newest first
                nextToken = result.nextContinuationToken
            )
        } catch (e: Exception) {
            Log.e("S3GalleryLoader", "❌ Failed to load photo page", e)
            return@withContext PageResult(emptyList(), null)
        }
    }
}

