package com.example.famlinks.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.famlinks.data.remote.s3.AwsS3Client
import com.example.famlinks.data.remote.s3.S3GalleryLoader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.famlinks.data.remote.s3.S3Photo
import kotlinx.coroutines.launch

class GalleryViewModel : ViewModel() {
    private val _photoList = MutableStateFlow<List<S3Photo>>(emptyList())
    val photoList: StateFlow<List<S3Photo>> = _photoList

    fun refreshGallery(context: Context, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                AwsS3Client.initialize(context)
                val urls = S3GalleryLoader.listPhotoUrls()
                val reversedList = urls.reversed()
                val photoObjects = reversedList.map { url ->
                    val key = url.substringBefore("?").substringAfter("users/")
                    S3Photo(key = "users/$key", url = url)
                }
                setPhotoList(photoObjects)
                Log.d("GalleryViewModel", "🔄 Refreshed photo list: ${photoObjects.size} items")
            } catch (e: Exception) {
                Log.e("GalleryViewModel", "❌ Failed to refresh photo list", e)
            } finally {
                onComplete()
            }
        }
    }
    fun setPhotoList(list: List<S3Photo>) {
        _photoList.value = list
    }

    fun isLoaded(): Boolean {
        return _photoList.value.isNotEmpty()
    }
}
