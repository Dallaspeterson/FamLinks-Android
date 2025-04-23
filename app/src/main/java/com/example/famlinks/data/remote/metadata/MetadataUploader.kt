// File: data/remote/metadata/MetadataUploader.kt
package com.example.famlinks.data.remote.metadata

import android.content.Context
import android.util.Log
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.example.famlinks.data.remote.s3.Visibility
import com.example.famlinks.util.GuestCredentialsProvider
import com.example.famlinks.util.UserIdProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object MetadataUploader {

    suspend fun uploadMetadata(context: Context, item: DynamoMetadataItem) = withContext(Dispatchers.IO) {
        try {
            val credentials = GuestCredentialsProvider.getCredentialsProvider(context)
            val dynamoDBClient = AmazonDynamoDBClient(credentials)
            val mapper = DynamoDBMapper(dynamoDBClient)

            mapper.save(item, DynamoDBMapperConfig.DEFAULT)
            Log.d("MetadataUploader", "✅ Metadata uploaded for ${item.photoKey}")
        } catch (e: Exception) {
            Log.e("MetadataUploader", "❌ Failed to upload metadata", e)
        }
    }

    // 🆕 Convenient helper to create and upload metadata with all fields
    suspend fun uploadFullMetadata(
        context: Context,
        s3Key: String,
        resolution: String?,
        fileSizeBytes: Long,
        mediaType: String = "photo",
        isSingle: Boolean = false,
        albumId: String? = null,
        portalId: String? = null,
        collectionIds: List<String> = emptyList(),
        visibility: Visibility = Visibility.PRIVATE,
        sharedWith: List<String> = emptyList(),
        latitude: Double? = null,
        longitude: Double? = null
    )     = withContext(Dispatchers.IO) {
        val userId = UserIdProvider.getUserId(context)
        val item = DynamoMetadataItem().apply {
            identityId = userId
            photoKey = s3Key
            timestamp = System.currentTimeMillis()
            this.latitude = latitude
            this.longitude = longitude
            this.fileSizeBytes = fileSizeBytes
            this.resolution = resolution
            this.mediaType = mediaType
            this.isSingle = isSingle
            this.albumId = albumId
            this.portalId = portalId
            this.collectionIds = collectionIds
            this.visibility = visibility.name
            this.sharedWith = sharedWith
            this.ownerId = userId
        }

        uploadMetadata(context, item)
    }
}

