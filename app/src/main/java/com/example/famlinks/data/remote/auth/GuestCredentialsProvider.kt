// File: util/GuestCredentialsProvider.kt
package com.example.famlinks.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.cognitoidentity.CognitoIdentityClient
import software.amazon.awssdk.services.cognitoidentity.model.GetIdRequest
import software.amazon.awssdk.services.cognitoidentity.model.GetCredentialsForIdentityRequest
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest

object GuestCredentialsProvider {
    private const val REGION = "us-east-1"
    private const val IDENTITY_POOL_ID = "us-east-1:86b4862f-914d-4d18-8bbc-427519285dc4"
    private const val PREFS_NAME = "famlinks_prefs"
    private const val KEY_IDENTITY_ID = "identity_id"
    private const val BUCKET_NAME = "famlinks-user-media"

    var identityId: String? = null
        private set

    fun getCredentialsProvider(context: Context): AwsCredentialsProvider {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedId = prefs.getString(KEY_IDENTITY_ID, null)

        val identityClient = CognitoIdentityClient.builder()
            .region(Region.of(REGION))
            .credentialsProvider(AnonymousCredentialsProvider.create())
            .httpClientBuilder(UrlConnectionHttpClient.builder())
            .build()

        val id = savedId?.also {
            Log.i("GuestCredentials", "🔄 Reusing saved Identity ID: $it")
        } ?: identityClient.getId(
            GetIdRequest.builder()
                .identityPoolId(IDENTITY_POOL_ID)
                .build()
        ).identityId().also {
            prefs.edit().putString(KEY_IDENTITY_ID, it).apply()
            Log.i("GuestCredentials", "✨ Fetched new Identity ID: $it")
        }

        identityId = id

// 🆕 Always fetch fresh session credentials after setting identityId
        val credentials = identityClient.getCredentialsForIdentity(
            GetCredentialsForIdentityRequest.builder()
                .identityId(id)
                .build()
        ).credentials()

        val sessionCredentials = AwsSessionCredentials.create(
            credentials.accessKeyId(),
            credentials.secretKey(),
            credentials.sessionToken()
        )

        Log.i("GuestCredentials", "✅ Got temporary session credentials")

        // ✅ Create hot storage folder with .keep marker
        try {
            val s3 = S3Client.builder()
                .region(Region.of(REGION))
                .credentialsProvider(StaticCredentialsProvider.create(sessionCredentials))
                .httpClientBuilder(UrlConnectionHttpClient.builder())
                .build()

            val folderKey = "users/$id/.keep"
            s3.putObject(
                PutObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(folderKey)
                    .build(),
                RequestBody.empty()
            )
            Log.i("GuestCredentials", "📂 Ensured S3 folder exists for user: $folderKey")
        } catch (e: Exception) {
            Log.w("GuestCredentials", "⚠️ Failed to create .keep in S3", e)
        }

        return StaticCredentialsProvider.create(sessionCredentials)
    }

    fun getIdentityId(context: Context): String? {
        if (identityId != null) return identityId
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_IDENTITY_ID, null)
    }
}

