// File: ui/gallery/GalleryScreen.kt
package com.example.famlinks.ui.gallery

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.famlinks.data.remote.s3.AwsS3Client
import com.example.famlinks.data.remote.s3.S3GalleryLoader
import com.example.famlinks.viewmodel.GalleryViewModel
import com.example.famlinks.viewmodel.PhotoViewerViewModel
import com.example.famlinks.data.remote.s3.S3Photo
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@Composable
fun GalleryScreen(
    navController: NavController,
    viewModel: PhotoViewerViewModel,
    galleryViewModel: GalleryViewModel,
    onPhotoClick: (Int) -> Unit
) {
    val context = LocalContext.current
    val photoList by galleryViewModel.photoList.collectAsState()
    var isLoading by remember { mutableStateOf(!galleryViewModel.isLoaded()) }
    var refreshing by remember { mutableStateOf(false) }

    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = refreshing)

    fun refreshGallery() {
        refreshing = true
        galleryViewModel.refreshGallery(context) {
            refreshing = false
        }
    }

    LaunchedEffect(Unit) {
        if (!galleryViewModel.isLoaded()) {
            isLoading = true
            refreshGallery()
        }
    }

    when {
        isLoading && photoList.isEmpty() -> {
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
                    columns = GridCells.Adaptive(minSize = 128.dp),
                    contentPadding = PaddingValues(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(photoList) { index, photo ->
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clickable {
                                    Log.d("GalleryScreen", "🖼️ Tapped image $index")
                                    viewModel.setPhotos(photoList)
                                    onPhotoClick(index)
                                }
                        ) {
                            val painter = rememberAsyncImagePainter(
                                ImageRequest.Builder(context)
                                    .data(photo.url)
                                    .crossfade(true)
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
