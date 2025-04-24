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

    private val allPhotos = mutableListOf<S3Photo>()
    private var continuationToken: String? = null
    private var isLoading = false
    private var isEndOfList = false
    private var isInitialized = false

    private var currentFilter: PhotoFilterType = PhotoFilterType.ALL

    fun loadNextPage(
        context: Context,
        filter: PhotoFilterType = currentFilter,
        pageSize: Int = 50,
        onComplete: () -> Unit = {}
    ) {
        if (isLoading || isEndOfList) return

        currentFilter = filter

        viewModelScope.launch {
            isLoading = true
            try {
                AwsS3Client.initialize(context)

                val page = S3GalleryLoader.loadPhotoPage(
                    context = context,
                    maxKeys = pageSize,
                    continuationToken = continuationToken
                )

                continuationToken = page.nextToken
                if (page.photos.isEmpty()) {
                    isEndOfList = true
                    Log.d("GalleryViewModel", "📭 No more photos to load.")
                } else {
                    Log.d("GalleryViewModel", "📷 Loaded ${page.photos.size} photos")
                    val newItems = page.photos.filter { it.key !in allPhotos.map { p -> p.key } }
                    allPhotos.addAll(newItems)
                    _photoList.value = applyFilter(allPhotos, currentFilter)
                }

                isInitialized = true
            } catch (e: Exception) {
                Log.e("GalleryViewModel", "❌ Failed to load photo page", e)
            } finally {
                isLoading = false
                onComplete()
            }
        }
    }

    fun refresh(context: Context, filter: PhotoFilterType = currentFilter, onComplete: () -> Unit = {}) {
        continuationToken = null
        isEndOfList = false
        isInitialized = false
        allPhotos.clear()
        _photoList.value = emptyList()
        loadNextPage(context, filter, onComplete = onComplete)
    }

    fun isLoaded(): Boolean = isInitialized

    fun markAsStale() {
        continuationToken = null
        isEndOfList = false
        isInitialized = false
        allPhotos.clear()
        _photoList.value = emptyList()
    }

    fun getFilteredPhotoList(filter: PhotoFilterType): List<S3Photo> {
        return applyFilter(_photoList.value, filter)
    }

    private fun applyFilter(photos: List<S3Photo>, filter: PhotoFilterType): List<S3Photo> {
        return photos.filter { photo ->
            when (filter) {
                PhotoFilterType.ALL -> true
                PhotoFilterType.SINGLES -> photo.isSingle
                PhotoFilterType.ALBUMS -> photo.albumId != null
                PhotoFilterType.COLLECTIONS -> photo.collectionIds.isNotEmpty()
                PhotoFilterType.PORTALS -> photo.portalId != null
            }
        }
    }
}



