// File: data/remote/s3/AwsS3Client.kt
package com.example.famlinks.data.remote.s3

import android.content.Context
import android.util.Log
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtilityOptions
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.regions.Regions
import com.example.famlinks.util.GuestCredentialsProvider

object AwsS3Client {
    private var s3Client: AmazonS3? = null
    private var transferUtility: TransferUtility? = null
    private var initialized = false
    private var identityId: String? = null

    suspend fun initialize(context: Context) {
        if (initialized) return

        try {
            val credentialsProvider = GuestCredentialsProvider.getCredentialsProvider(context)
            identityId = credentialsProvider.identityId

            s3Client = AmazonS3Client(credentialsProvider, com.amazonaws.regions.Region.getRegion(Regions.US_EAST_1))

            transferUtility = TransferUtility.builder()
                .context(context)
                .s3Client(s3Client)
                .transferUtilityOptions(TransferUtilityOptions())
                .build()

            initialized = true
            Log.i("AwsS3Client", "✅ S3 initialized for user: $identityId")
        } catch (e: Exception) {
            Log.e("AwsS3Client", "❌ Failed to initialize S3", e)
        }
    }

    fun getClient(): AmazonS3? = s3Client
    fun getUtils(): TransferUtility? = transferUtility
    fun getIdentityId(): String? = identityId

    fun getBucketName(): String = "famlinks-user-media"
}

