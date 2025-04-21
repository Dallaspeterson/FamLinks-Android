// File: viewmodel/GalleryViewModel.kt
package com.example.famlinks.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.famlinks.data.remote.s3.AwsS3Client
import com.example.famlinks.data.remote.s3.S3GalleryLoader
import com.example.famlinks.data.remote.s3.S3Photo
import com.example.famlinks.model.PhotoFilterType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GalleryViewModel : ViewModel() {
    private val _photoList = MutableStateFlow<List<S3Photo>>(emptyList())
    val photoList: StateFlow<List<S3Photo>> = _photoList

    private val cachedPhotos = mutableMapOf<PhotoFilterType, List<S3Photo>>()

    fun refreshGallery(
        context: Context,
        filter: PhotoFilterType = PhotoFilterType.ALL,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                AwsS3Client.initialize(context)
                val urls = S3GalleryLoader.listPhotoUrls(context)
                val reversedList = urls.reversed()
                val allPhotos = reversedList.map { url ->
                    val key = url.substringBefore("?").substringAfter("users/")
                    S3Photo(key = "users/$key", url = url)
                }

                val filtered = when (filter) {
                    PhotoFilterType.ALL -> allPhotos
                    PhotoFilterType.MOMENTS -> allPhotos.filter { it.key.contains("moment") }
                    PhotoFilterType.MEMORIES -> allPhotos.filter { it.key.contains("memory") }
                    PhotoFilterType.PORTALS -> allPhotos.filter { it.key.contains("portal") }
                }

                val currentCache = cachedPhotos[filter].orEmpty()

                if (filtered.size > currentCache.size) {
                    val newItems = filtered.subtract(currentCache.toSet())
                    val updatedList = currentCache + newItems
                    cachedPhotos[filter] = updatedList
                    _photoList.value = updatedList
                    Log.d("GalleryViewModel", "🆕 ${newItems.size} new photo(s) added.")
                } else {
                    Log.d("GalleryViewModel", "✅ No new photos. Using cache.")
                    _photoList.value = currentCache
                }
            } catch (e: Exception) {
                Log.e("GalleryViewModel", "❌ Failed to refresh photo list", e)
            } finally {
                onComplete()
            }
        }
    }

    fun setPhotoList(list: List<S3Photo>, filter: PhotoFilterType = PhotoFilterType.ALL) {
        cachedPhotos[filter] = list
        _photoList.value = list
    }

    fun isLoaded(): Boolean = _photoList.value.isNotEmpty()
}

