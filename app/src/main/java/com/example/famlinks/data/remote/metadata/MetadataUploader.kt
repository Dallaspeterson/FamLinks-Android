package com.example.famlinks.data.remote.metadata

import android.util.Log
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.example.famlinks.util.GuestCredentialsProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.content.Context

object MetadataUploader {

    suspend fun uploadMetadata(context: Context, metadata: PhotoMetadata) = withContext(Dispatchers.IO) {
        try {
            val credentials = GuestCredentialsProvider.getCredentialsProvider(context)
            val dynamoDBClient = AmazonDynamoDBClient(credentials)
            val mapper = DynamoDBMapper(dynamoDBClient)

            val item = DynamoMetadataItem().apply {
                identityId = metadata.identityId
                photoKey = metadata.photoKey
                timestamp = metadata.timestamp
                latitude = metadata.latitude
                longitude = metadata.longitude
            }

            mapper.save(item, DynamoDBMapperConfig.DEFAULT)
            Log.d("MetadataUploader", "Metadata uploaded for ${metadata.photoKey}")
        } catch (e: Exception) {
            Log.e("MetadataUploader", "Failed to upload metadata", e)
        }
    }
}
