// File: data/remote/s3/S3Uploader.kt
package com.example.famlinks.data.remote.s3

import android.content.Context
import android.util.Log
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import com.example.famlinks.util.GuestCredentialsProvider
import java.io.File

object S3Uploader {
    private const val REGION = "us-east-1"
    private const val BUCKET_NAME = "famlinks-user-media"

    suspend fun uploadPhoto(context: Context, file: File): Boolean {
        return try {
            val credentialsProvider = GuestCredentialsProvider.getCredentialsProvider(context)
            val identityId = GuestCredentialsProvider.getIdentityId(context) ?: run {
                Log.e("S3Uploader", "❌ Missing identity ID")
                return false
            }

            val key = "users/$identityId/${file.name}"
            Log.i("S3Uploader", "📤 Uploading to bucket: $BUCKET_NAME, Key: $key")

            val s3 = S3Client.builder()
                .region(Region.of(REGION))
                .credentialsProvider(credentialsProvider)
                .httpClientBuilder(UrlConnectionHttpClient.builder())
                .build()

            val request = PutObjectRequest.builder()
                .bucket(BUCKET_NAME)
                .key(key)
                .build()

            val requestBody = RequestBody.fromFile(file)

            val response = s3.putObject(request, requestBody)
            Log.i("S3Uploader", "✅ Upload completed. ETag: ${response.eTag()}")
            true
        } catch (e: Exception) {
            Log.e("S3Uploader", "❌ Upload failed", e)
            false
        }
    }
}

