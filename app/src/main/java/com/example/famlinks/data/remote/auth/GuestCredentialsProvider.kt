// File: util/GuestCredentialsProvider.kt
package com.example.famlinks.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import java.io.ByteArrayInputStream

object GuestCredentialsProvider {
    private val REGION = Regions.US_EAST_1
    private const val IDENTITY_POOL_ID = "us-east-1:86b4862f-914d-4d18-8bbc-427519285dc4"
    private const val PREFS_NAME = "famlinks_prefs"
    private const val KEY_IDENTITY_ID = "identity_id"
    private const val BUCKET_NAME = "famlinks-user-media"

    var identityId: String? = null
        private set

    fun getCredentialsProvider(context: Context): CognitoCachingCredentialsProvider {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val provider = CognitoCachingCredentialsProvider(
            context,
            IDENTITY_POOL_ID,
            REGION
        )

        identityId = provider.identityId.also {
            Log.i("GuestCredentials", "✅ Got Identity ID: $it")
            prefs.edit().putString(KEY_IDENTITY_ID, it).apply()
        }

        // Optional: ensure S3 folder exists with `.keep` file
        try {
            val s3 = AmazonS3Client(provider)
            val folderKey = "users/$identityId/.keep"
            val emptyStream = ByteArrayInputStream(ByteArray(0))
            val metadata = ObjectMetadata().apply { contentLength = 0 }

            val putRequest = PutObjectRequest(BUCKET_NAME, folderKey, emptyStream, metadata)
            s3.putObject(putRequest)

            Log.i("GuestCredentials", "📂 Ensured S3 folder exists: $folderKey")
        } catch (e: Exception) {
            Log.w("GuestCredentials", "⚠️ Failed to create .keep file", e)
        }

        return provider
    }

    fun getIdentityId(context: Context): String? {
        if (identityId != null) return identityId
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_IDENTITY_ID, null)
    }
}

