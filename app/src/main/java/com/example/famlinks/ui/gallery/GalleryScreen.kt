// File: ui/gallery/GalleryScreen.kt
package com.example.famlinks.ui.gallery

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.famlinks.data.remote.s3.S3GalleryLoader
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen() {
    val context = LocalContext.current
    var s3ImageUrls by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val reloadTrigger = remember { mutableStateOf(0) } // triggers reload when value changes

    LaunchedEffect(reloadTrigger.value) {
        isLoading = true
        val urls = S3GalleryLoader.listPhotoUrls()
        // TEST: Upload dummy metadata to DynamoDB
        val identityId = com.example.famlinks.util.GuestCredentialsProvider.getIdentityId(context)

        if (identityId != null) {
            val testMetadata = com.example.famlinks.data.remote.metadata.PhotoMetadata(
                identityId = identityId,
                photoKey = "users/$identityId/test-photo.jpg",
                timestamp = System.currentTimeMillis(),
                latitude = 33.1234,
                longitude = -110.5678
            )

            com.example.famlinks.data.remote.metadata.MetadataUploader.uploadMetadata(context, testMetadata)
        }

        Log.d("GalleryScreen", "✅ Loaded ${urls.size} image URLs")
        s3ImageUrls = urls
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gallery") },
                actions = {
                    IconButton(onClick = {
                        // Manual reload option
                        reloadTrigger.value++
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reload")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                s3ImageUrls.isEmpty() -> {
                    Text(
                        "No photos uploaded yet.",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 120.dp),
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(s3ImageUrls) { url ->
                            Image(
                                painter = rememberAsyncImagePainter(url),
                                contentDescription = null,
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clickable {
                                        Toast
                                            .makeText(context, "Tapped: $url", Toast.LENGTH_SHORT)
                                            .show()
                                    }
                            )
                        }
                    }
                }
            }
        }
    }
}

