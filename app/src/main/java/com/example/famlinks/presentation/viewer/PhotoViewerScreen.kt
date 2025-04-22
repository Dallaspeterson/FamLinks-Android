// File: presentation/viewer/PhotoViewerScreen.kt
package com.example.famlinks.presentation.viewer

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.famlinks.R
import com.example.famlinks.data.analytics.UsageTracker
import com.example.famlinks.util.GuestCredentialsProvider
import com.example.famlinks.viewmodel.PhotoDisplayMetadata
import com.example.famlinks.viewmodel.PhotoViewerViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PhotoViewerScreen(
    navController: NavController,
    initialIndex: Int,
    viewModel: PhotoViewerViewModel,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val photoList by viewModel.photoList.collectAsState()
    val metadataMap by viewModel.metadataMap.collectAsState()
    val pagerState = rememberPagerState(
        initialPage = initialIndex,
        pageCount = { photoList.size }
    )
    var showMetadata by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = {
                    Text("Photo ${pagerState.currentPage + 1} of ${photoList.size}")
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        showMetadata = true
                        viewModel.getPhoto(pagerState.currentPage)?.let { photo ->
                            viewModel.loadMetadata(context, photo.key)
                        }
                    }) {
                        Icon(Icons.Filled.Info, contentDescription = "Photo Info")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.Black)
        ) {
            if (photoList.isNotEmpty()) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    val photo = viewModel.getPhoto(page)
                    Log.d("PhotoViewerScreen", "📸 Displaying URL: ${photo?.url}")

                    photo?.let {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(it.url)
                                    .crossfade(true)
                                    .error(R.drawable.image_load_error)
                                    .build(),
                                contentDescription = "Photo",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                            LaunchedEffect(photo.key) {
                                val estimatedSizeBytes = if (photo.url.contains("cloudfront")) {
                                    500_000L // Cold compressed
                                } else {
                                    3_000_000L // Hot full-res
                                }
                                UsageTracker.logView(
                                    userId = GuestCredentialsProvider.getIdentityId(context),
                                    mediaType = "photo",
                                    estimatedSizeBytes = estimatedSizeBytes,
                                    tier = if (photo.url.contains("cloudfront")) "cold" else "hot"
                                )
                            }
                        }
                    }
                }
            } else {
                Log.w("PhotoViewerScreen", "🛑 photoList is empty!")
                Text(
                    text = "No photos to display.",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            if (showMetadata) {
                viewModel.getPhoto(pagerState.currentPage)?.let { photo ->
                    metadataMap[photo.key]?.let { metadata ->
                        PhotoMetadataSheet(metadata) { showMetadata = false }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoMetadataSheet(metadata: PhotoDisplayMetadata, onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            InfoRow("📅 Date Taken", metadata.dateTaken)
            InfoRow("📁 File Size", metadata.fileSize)
            InfoRow("📀 Resolution", metadata.resolution)
            InfoRow("📍 Location", metadata.location)
            InfoRow("🖼️ Type", metadata.mediaType)
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text("$label: ", style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

