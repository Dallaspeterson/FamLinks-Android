// File: util/GuestCredentialsProvider.kt
package com.example.famlinks.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.cognitoidentity.CognitoIdentityClient
import software.amazon.awssdk.services.cognitoidentity.model.GetIdRequest
import software.amazon.awssdk.services.cognitoidentity.model.GetCredentialsForIdentityRequest

object GuestCredentialsProvider {
    private const val REGION = "us-east-1"
    private const val IDENTITY_POOL_ID = "us-east-1:86b4862f-914d-4d18-8bbc-427519285dc4" // Replace with your real Identity Pool ID
    private const val PREFS_NAME = "famlinks_prefs"
    private const val KEY_IDENTITY_ID = "identity_id"

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

        val id = savedId ?: identityClient.getId(
            GetIdRequest.builder()
                .identityPoolId(IDENTITY_POOL_ID)
                .build()
        ).identityId().also {
            prefs.edit().putString(KEY_IDENTITY_ID, it).apply()
            Log.i("GuestCredentials", "✨ Fetched new Identity ID: $it")
        }

        identityId = id

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

        return StaticCredentialsProvider.create(sessionCredentials)
    }

    fun getIdentityId(context: Context): String? {
        if (identityId != null) return identityId
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_IDENTITY_ID, null)
    }
}
