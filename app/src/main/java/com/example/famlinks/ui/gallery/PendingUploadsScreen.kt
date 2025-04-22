// File: ui/gallery/PendingUploadsScreen.kt
package com.example.famlinks.ui.gallery

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.famlinks.model.UploadStatus
import com.example.famlinks.util.AppPreferences
import com.example.famlinks.viewmodel.GalleryViewModel
import com.example.famlinks.viewmodel.PendingUploadsViewModel

@Composable
fun PendingUploadsScreen(
    navController: NavHostController,
    pendingUploadsViewModel: PendingUploadsViewModel,
    galleryViewModel: GalleryViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val pendingUploads by pendingUploadsViewModel.pendingUploads.collectAsState()

    LaunchedEffect(Unit) {
        pendingUploadsViewModel.loadFromDisk(context)

        val allowCellular = AppPreferences.isDataAllowed(context)
        com.example.famlinks.data.upload.UploadManager.startUploading(
            context = context,
            viewModel = pendingUploadsViewModel,
            allowCellular = allowCellular,
            galleryViewModel = galleryViewModel
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "\uD83D\uDCE4 Pending Uploads",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(8.dp)
            )

            // Toggle Wi-Fi-only uploads (keep this if you already had it)
            val wifiOnly = remember { mutableStateOf(AppPreferences.isWifiOnlyUploadsEnabled(context)) }
            Text("wifi only")
            Switch(
                checked = wifiOnly.value,
                onCheckedChange = {
                    wifiOnly.value = it
                    AppPreferences.setWifiOnlyUploadsEnabled(context, it)
                }
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 128.dp),
            contentPadding = PaddingValues(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(pendingUploads, key = { it.id }) { item ->
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .aspectRatio(1f)
                ) {
                    val painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(context)
                            .data(item.localPath)
                            .crossfade(true)
                            .build()
                    )

                    Image(
                        painter = painter,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // 👇 Upload progress overlay
                    if (item.uploadStatus == UploadStatus.UPLOADING) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                strokeWidth = 6.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
