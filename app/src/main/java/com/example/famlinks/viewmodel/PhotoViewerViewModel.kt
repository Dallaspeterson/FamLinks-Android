// File: viewmodel/PhotoViewerViewModel.kt
package com.example.famlinks.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.example.famlinks.data.remote.metadata.DynamoMetadataItem
import com.example.famlinks.data.remote.s3.S3Photo
import com.example.famlinks.util.GuestCredentialsProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class PhotoDisplayMetadata(
    val dateTaken: String,
    val fileSize: String,
    val resolution: String,
    val location: String
)

class PhotoViewerViewModel : ViewModel() {

    private val _metadataMap = MutableStateFlow<Map<String, PhotoDisplayMetadata>>(emptyMap())
    val metadataMap: StateFlow<Map<String, PhotoDisplayMetadata>> = _metadataMap.asStateFlow()

    private val _photoList = MutableStateFlow<List<S3Photo>>(emptyList())
    val photoList: StateFlow<List<S3Photo>> = _photoList.asStateFlow()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

    fun setPhotos(photos: List<S3Photo>) {
        _photoList.value = photos
        Log.d("PhotoViewerViewModel", "📷 Set ${photos.size} photo(s)")
    }

    fun getPhoto(index: Int): S3Photo? = _photoList.value.getOrNull(index)

    fun loadMetadata(context: Context, photoKey: String) {
        if (_metadataMap.value.containsKey(photoKey)) {
            Log.d("PhotoViewerViewModel", "📎 Metadata already loaded for $photoKey")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("PhotoViewerViewModel", "📥 Loading metadata for key: $photoKey")

                val identityId = GuestCredentialsProvider.getIdentityId(context)
                val keyObject = DynamoMetadataItem().apply {
                    this.identityId = identityId
                    this.photoKey = photoKey
                }

                val credentials = GuestCredentialsProvider.getCredentialsProvider(context)
                val dynamoDBClient = AmazonDynamoDBClient(credentials)
                val mapper = DynamoDBMapper(dynamoDBClient)

                val item = mapper.load(DynamoMetadataItem::class.java, identityId, photoKey)

                if (item != null) {
                    val dateTaken = item.timestamp?.let { dateFormat.format(Date(it)) } ?: "Unknown Date"

                    val location = if (item.latitude != null && item.longitude != null) {
                        "Lat: ${item.latitude}, Lon: ${item.longitude}"
                    } else {
                        "Location Unavailable"
                    }

                    val metadata = PhotoDisplayMetadata(
                        dateTaken = dateTaken,
                        fileSize = "Unknown",
                        resolution = "Unknown",
                        location = location
                    )

                    _metadataMap.update { currentMap -> currentMap + (photoKey to metadata) }

                    Log.d("PhotoViewerViewModel", "✅ Metadata loaded for $photoKey")
                } else {
                    Log.e("PhotoViewerViewModel", "⚠️ Metadata not found for key: $photoKey")
                }
            } catch (e: Exception) {
                Log.e("PhotoViewerViewModel", "❌ Metadata load failed for $photoKey", e)
            }
        }
    }
}
