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
}

