// File: ui/gallery/PendingUploadsScreen.kt
// File: ui/gallery/PendingUploadsScreen.kt
package com.example.famlinks.ui.gallery

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
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
import com.example.famlinks.viewmodel.PendingUploadsViewModel

@Composable
fun PendingUploadsScreen(
    navController: NavController,
    pendingUploadsViewModel: PendingUploadsViewModel
) {
    val context = LocalContext.current
    val pendingUploads by pendingUploadsViewModel.pendingUploads.collectAsState()

    LaunchedEffect(Unit) {
        pendingUploadsViewModel.loadFromDisk(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Text(
            text = "\uD83D\uDCE4 Pending Uploads",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(8.dp)
        )

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
                        .clickable {
                            // Optional: Add click behavior if needed
                        }
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
                }
            }
        }
    }
}

