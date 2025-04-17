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

    LaunchedEffect(Unit) {
        if (!galleryViewModel.isLoaded()) {
            try {
                Log.d("GalleryScreen", "🛠 Initializing S3 client")
                AwsS3Client.initialize(context)
                Log.d("GalleryScreen", "✅ S3 initialized. Loading gallery...")
                val urls = S3GalleryLoader.listPhotoUrls()

                val reversedList = urls.reversed()
                val photoObjects = reversedList.map { url ->
                    val key = url.substringBefore("?").substringAfter("users/")
                    S3Photo(key = "users/$key", url = url)
                }

                galleryViewModel.setPhotoList(photoObjects)
                Log.d("GalleryScreen", "📷 Loaded ${photoObjects.size} images.")
            } catch (e: Exception) {
                Log.e("GalleryScreen", "❌ Failed to load gallery", e)
            } finally {
                isLoading = false
            }
        }
    }

    when {
        isLoading -> {
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