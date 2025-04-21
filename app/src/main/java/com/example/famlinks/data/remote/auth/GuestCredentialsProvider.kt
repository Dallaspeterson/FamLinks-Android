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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream

object GuestCredentialsProvider {
    private val REGION = Regions.US_EAST_1
    private const val IDENTITY_POOL_ID = "us-east-1:86b4862f-914d-4d18-8bbc-427519285dc4"
    private const val PREFS_NAME = "famlinks_prefs"
    private const val KEY_IDENTITY_ID = "identity_id"
    private const val BUCKET_NAME = "famlinks-user-media"

    var identityId: String? = null
        private set

    // ⚙️ Try to initialize AWS credentials + ensure S3 folder
    suspend fun getCredentialsProvider(context: Context): CognitoCachingCredentialsProvider = withContext(Dispatchers.IO) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val provider = CognitoCachingCredentialsProvider(
            context,
            IDENTITY_POOL_ID,
            REGION
        )

        try {
            // Try to get Cognito Identity ID
            identityId = provider.identityId.also {
                Log.i("GuestCredentials", "✅ Got Identity ID: $it")
                prefs.edit().putString(KEY_IDENTITY_ID, it).apply()
                UserIdProvider.saveCloudUserId(context, it)
            }

            // Optional: ensure S3 folder exists
            val s3 = AmazonS3Client(provider)
            val folderKey = "users/$identityId/.keep"
            val emptyStream = ByteArrayInputStream(ByteArray(0))
            val metadata = ObjectMetadata().apply { contentLength = 0 }
            val putRequest = PutObjectRequest(BUCKET_NAME, folderKey, emptyStream, metadata)
            s3.putObject(putRequest)

            Log.i("GuestCredentials", "📂 Ensured S3 folder exists: $folderKey")
        } catch (e: Exception) {
            Log.w("GuestCredentials", "⚠️ Failed to get Cognito identity or create .keep file (offline?)", e)
            identityId = null // fall back will be triggered in getIdentityId
        }

        return@withContext provider
    }

    // 🧠 Get identity ID with full offline fallback
    suspend fun getIdentityId(context: Context): String = withContext(Dispatchers.IO) {
        identityId ?: runCatching {
            getCredentialsProvider(context).identityId
        }.onSuccess {
            identityId = it
        }.onFailure {
            Log.w("GuestCredentials", "⚠️ Falling back to local UUID for identity", it)
            identityId = UserIdProvider.getUserId(context)
        }

        return@withContext identityId ?: UserIdProvider.getUserId(context)
    }

    fun getIdentityIdFromPrefs(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_IDENTITY_ID, null)
    }
}
