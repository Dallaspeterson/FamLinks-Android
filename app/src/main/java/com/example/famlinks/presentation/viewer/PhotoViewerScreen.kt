// File: presentation/viewer/PhotoViewerScreen.kt
package com.example.famlinks.presentation.viewer

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.famlinks.viewmodel.PhotoViewerViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoViewerScreen(
    navController: NavHostController,
    viewModel: PhotoViewerViewModel,
    initialIndex: Int,
    onClose: () -> Unit
) {
    val photoList by viewModel.photoList.collectAsState()
    val metadataMap by viewModel.metadataMap.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var currentIndex by remember { mutableStateOf(initialIndex) }
    val currentPhoto = photoList.getOrNull(currentIndex)

    val bottomSheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    LaunchedEffect(currentPhoto) {
        currentPhoto?.let {
            viewModel.loadMetadata(context, it.key)
        }
    }

    LaunchedEffect(showBottomSheet) {
        if (showBottomSheet) {
            scope.launch { bottomSheetState.show() }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(onTap = { showBottomSheet = true })
            }
    ) {
        currentPhoto?.let { photo ->
            val coldUrl = remember(currentPhoto.key) {
                generateColdUrl(currentPhoto.key, context)
            }

            var hasLoggedView by remember { mutableStateOf(false) }

            val painter = rememberAsyncImagePainter(
                model = coil.request.ImageRequest.Builder(context)
                    .data(coldUrl)
                    .crossfade(true)
                    .listener(
                        onSuccess = { _, _ ->
                            if (!hasLoggedView) {
                                try {
                                    val userId = com.example.famlinks.util.UserIdProvider.getUserId(context)
                                    photo.sizeBytes?.let {
                                        com.example.famlinks.data.analytics.UsageTracker.logView(
                                            userId = userId,
                                            mediaType = photo.mediaType,
                                            estimatedSizeBytes = it,
                                            tier = "cold"
                                        )
                                        hasLoggedView = true
                                    }
                                } catch (e: Exception) {
                                    Log.e("PhotoViewerScreen", "⚠️ Failed to log view: ${e.localizedMessage}")
                                }
                            }
                        }
                    )
                    .build()
            )

            val isLoading = painter.state is coil.compose.AsyncImagePainter.State.Loading
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White
                )
            }

            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        }

    IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
        }

        IconButton(
            onClick = { showBottomSheet = true },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Info, contentDescription = "Info", tint = Color.White)
        }

        if (showBottomSheet && currentPhoto != null) {
            val correctedKey = currentPhoto.key
                .replace("/preview/", "/cold/")
                .replace("_thumb.jpg", "_1080p.jpg")

            val coldUrl = remember(currentPhoto.key) {
                generateColdUrl(currentPhoto.key, context)
            }


            val metadata = metadataMap[correctedKey]

            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = bottomSheetState
            ) {
                if (metadata != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Text("Photo Metadata", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Date Taken: ${metadata.dateTaken}")
                        Text("Resolution: ${metadata.resolution}")
                        Text("File Size: ${metadata.fileSize}")
                        Text("Location: ${metadata.location}")
                        Text("Media Type: ${metadata.mediaType}")
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                } else {
                    Text(
                        "No metadata found.",
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }
        }
    }
}
fun generateColdUrl(key: String, context: android.content.Context): String? {
    val correctedKey = key
        .replace("/preview/", "/cold/")
        .replace("_thumb.jpg", "_1080p.jpg")
    val client = com.example.famlinks.data.remote.s3.AwsS3Client.getClient()
    val bucket = com.example.famlinks.data.remote.s3.AwsS3Client.getBucketName()
    val expiration = java.util.Date(System.currentTimeMillis() + 60 * 60 * 1000) // 1 hour

    return client?.generatePresignedUrl(
        com.amazonaws.services.s3.model.GeneratePresignedUrlRequest(bucket, correctedKey)
            .withMethod(com.amazonaws.HttpMethod.GET)
            .withExpiration(expiration)
    )?.toString()
}
