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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.famlinks.data.remote.s3.AwsS3Client
import com.example.famlinks.data.remote.s3.S3GalleryLoader
import com.example.famlinks.viewmodel.PhotoViewerViewModel



@Composable
fun GalleryScreen(
    navController: NavController,
    viewModel: PhotoViewerViewModel
) {
    val context = LocalContext.current
    var photoUrls by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            Log.d("GalleryScreen", "🛠 Initializing S3 client")
            AwsS3Client.initialize(context)
            Log.d("GalleryScreen", "✅ S3 initialized. Loading gallery...")
            val urls = S3GalleryLoader.listPhotoUrls()
            photoUrls = urls
            Log.d("GalleryScreen", "📷 Loaded ${urls.size} images.")
        } catch (e: Exception) {
            Log.e("GalleryScreen", "❌ Failed to load gallery", e)
        } finally {
            isLoading = false
        }
    }

    when {
        isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        photoUrls.isEmpty() -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No photos found.")
            }
        }

        else -> {
            val originalList = photoUrls
            val reversedList = originalList.reversed()

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 128.dp),
                contentPadding = PaddingValues(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(reversedList) { _, url ->
                    val originalIndex = originalList.indexOf(url)
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clickable {
                                Log.d("GalleryScreen", "🖼️ Tapped image $originalIndex")
                                viewModel.setPhotos(originalList)
                                navController.navigate("photoViewer/$originalIndex")
                            }
                    ) {
                        val painter = rememberAsyncImagePainter(
                            ImageRequest.Builder(context)
                                .data(url)
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