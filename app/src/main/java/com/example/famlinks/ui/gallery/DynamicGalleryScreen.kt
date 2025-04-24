// File: ui/gallery/DynamicGalleryScreen.kt
package com.example.famlinks.ui.gallery

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.famlinks.viewmodel.GalleryViewModel
import com.example.famlinks.viewmodel.PhotoViewerViewModel
import com.example.famlinks.data.remote.s3.S3Photo
import com.example.famlinks.model.PhotoFilterType
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@Composable
fun DynamicGalleryScreen(
    navController: NavHostController,
    galleryViewModel: GalleryViewModel,
    photoViewerViewModel: PhotoViewerViewModel,
    onPhotoClick: (Int) -> Unit,
    filterType: PhotoFilterType,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val rawPhotos by galleryViewModel.photoList.collectAsState()
    val photoList = remember(rawPhotos, filterType) {
        galleryViewModel.getFilteredPhotoList(filterType)
    }
    LaunchedEffect(photoList) {
        Log.d("DynamicGalleryScreen", "📊 Filter: ${filterType.name}, Total: ${rawPhotos.size}, Filtered: ${photoList.size}")
        photoList.forEach {
            Log.d("DynamicGalleryScreen", "🔍 ${it.key} — isSingle=${it.isSingle}, albumId=${it.albumId}, portalId=${it.portalId}, collections=${it.collectionIds}")
        }
    }
    val gridState = rememberLazyGridState()

    var refreshing by remember { mutableStateOf(false) }
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = refreshing)

    LaunchedEffect(filterType) {
        if (!galleryViewModel.isLoaded()) {
            galleryViewModel.loadNextPage(context, filterType)
        }
    }

    fun refreshGallery() {
        refreshing = true
        galleryViewModel.markAsStale()
        galleryViewModel.loadNextPage(context, filterType) {
            refreshing = false
        }
    }

    LaunchedEffect(gridState.firstVisibleItemIndex, photoList.size) {
        val shouldLoadMore = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0 >= photoList.size - 6
        if (shouldLoadMore) {
            galleryViewModel.loadNextPage(context, filterType)
        }
    }

    when {
        photoList.isEmpty() && refreshing -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        photoList.isEmpty() -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No photos found.")
            }
        }

        else -> {
            SwipeRefresh(
                state = swipeRefreshState,
                onRefresh = { refreshGallery() }
            ) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier.fillMaxSize(),
                            state = gridState, // ✅ Keep this to track scroll position
                            userScrollEnabled = true
                        ) {
                            itemsIndexed(photoList, key = { _, photo -> photo.key }) { index, photo ->
                                Box(
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .fillMaxWidth()
                                        .aspectRatio(1f)
                                        .clickable {
                                            Log.d("DynamicGalleryScreen", "🖼️ Tapped image $index")
                                            photoViewerViewModel.setPhotos(photoList)
                                            onPhotoClick(index)
                                        }
                                ) {
                                    val painter = rememberAsyncImagePainter(
                                        ImageRequest.Builder(context)
                                            .data(photo.url)
                                            .crossfade(true)
                                            .diskCachePolicy(coil.request.CachePolicy.ENABLED) // ✅ Disk caching
                                            .memoryCachePolicy(coil.request.CachePolicy.ENABLED) // ✅ Memory caching
                                            .build()
                                    )

                                    Image(
                                        painter = painter,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }