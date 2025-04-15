// File: data/remote/s3/AwsS3Client.kt
package com.example.famlinks.data.remote.s3

import android.content.Context
import android.util.Log
import com.example.famlinks.util.GuestCredentialsProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3Utilities

object AwsS3Client {
    private const val REGION = "us-east-1"

    private var s3Client: S3Client? = null
    private var s3Utils: S3Utilities? = null
    private var identityId: String? = null
    private var initialized = false

    suspend fun initialize(context: Context) {
        if (initialized) return

        withContext(Dispatchers.IO) {
            try {
                val credentialsProvider = GuestCredentialsProvider.getCredentialsProvider(context)
                identityId = GuestCredentialsProvider.getIdentityId(context)

                s3Client = S3Client.builder()
                    .region(Region.of(REGION))
                    .credentialsProvider(credentialsProvider)
                    .httpClientBuilder(UrlConnectionHttpClient.builder())
                    .build()

                s3Utils = s3Client!!.utilities()
                initialized = true

                Log.i("AwsS3Client", "✅ Initialized S3 for $identityId")
            } catch (e: Exception) {
                Log.e("AwsS3Client", "❌ Failed to initialize S3", e)
            }
        }
    }
    fun getIdentityId(): String? {
        return GuestCredentialsProvider.identityId
    }
    fun getClient(): S3Client? = s3Client
    fun getUtils(): S3Utilities? = s3Utils
}

