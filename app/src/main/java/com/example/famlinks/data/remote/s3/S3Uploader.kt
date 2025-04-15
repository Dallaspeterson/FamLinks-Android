// File: data/remote/s3/S3Uploader.kt
package com.example.famlinks.data.remote.s3

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.io.File
import com.example.famlinks.data.remote.Constants

object S3Uploader {
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun uploadPhoto(file: File, userId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val key = "users/$userId/${file.name}"
            val s3 = AwsS3Client.getClient() ?: return@withContext false

            val request = PutObjectRequest.builder()
                .bucket(Constants.S3_BUCKET)
                .key(key)
                .build()

            s3.putObject(request, file.toPath())
            true
        } catch (e: Exception) {
            Log.e("S3Uploader", "Upload failed", e)
            false
        }
    }
}
