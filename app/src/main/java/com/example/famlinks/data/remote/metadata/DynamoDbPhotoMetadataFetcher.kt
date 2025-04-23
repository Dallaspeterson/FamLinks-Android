// File: data/remote/metadata/DynamoDbPhotoMetadataFetcher.kt
package com.example.famlinks.data.remote.metadata

import android.content.Context
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.example.famlinks.util.GuestCredentialsProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DynamoDbPhotoMetadataFetcher {

    suspend fun getMetadataForKey(context: Context, photoKey: String): DynamoMetadataItem? = withContext(Dispatchers.IO) {
        return@withContext try {
            val credentials = GuestCredentialsProvider.getCredentialsProvider(context)
            val dynamoDBClient = AmazonDynamoDBClient(credentials)
            val mapper = DynamoDBMapper(dynamoDBClient)

            val userId = com.example.famlinks.util.UserIdProvider.getUserId(context)

            mapper.load(
                DynamoMetadataItem::class.java,
                userId,
                photoKey,
                DynamoDBMapperConfig.DEFAULT
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
