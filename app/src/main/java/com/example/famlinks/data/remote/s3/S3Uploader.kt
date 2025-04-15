// File: data/remote/s3/S3Uploader.kt
package com.example.famlinks.data.remote.s3

import android.content.Context
import android.util.Log
import com.amazonaws.services.s3.model.PutObjectRequest
import java.io.File

object S3Uploader {
    private const val BUCKET_NAME = "famlinks-user-media"

    fun uploadPhoto(context: Context, file: File): Boolean {
        return try {
            val s3 = AwsS3Client.getClient() ?: run {
                Log.e("S3Uploader", "❌ S3 client not initialized")
                return false
            }

            val identityId = AwsS3Client.getIdentityId() ?: run {
                Log.e("S3Uploader", "❌ Identity ID not set")
                return false
            }

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
}

