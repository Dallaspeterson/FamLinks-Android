// File: ui/viewer/PhotoViewerScreen.kt
package com.example.famlinks.ui.viewer

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import android.location.Geocoder
import androidx.compose.ui.platform.LocalContext
import android.media.ExifInterface
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.ExperimentalFoundationApi

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PhotoViewerScreen(
    navController: NavController,
    photoPaths: List<String>,
    initialIndex: Int
) {
    val pagerState = rememberPagerState(
        initialPage = initialIndex,
        pageCount = { photoPaths.size }
    )

    var showMetadata by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Photo ${pagerState.currentPage + 1} of ${photoPaths.size}")
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showMetadata = true }) {
                        Icon(Icons.Filled.Info, contentDescription = "Photo Info")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val uri = photoPaths[page].toUri()

                Image(
                    painter = rememberAsyncImagePainter(uri),
                    contentDescription = "Photo",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )
            }

            if (showMetadata) {
                val currentPath = photoPaths[pagerState.currentPage]
                PhotoMetadataSheet(
                    photoPath = currentPath,
                    onDismiss = { showMetadata = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoMetadataSheet(photoPath: String, onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth()
    ) {
        val file = File(photoPath)
        val exif = remember(photoPath) {
            runCatching { ExifInterface(photoPath) }.getOrNull()
        }

        val dateTaken = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            .format(Date(file.lastModified()))

        val fileSizeKb = file.length() / 1024
        val fileSize = if (fileSizeKb > 1000) {
            val mb = fileSizeKb / 1024f
            String.format("%.1f MB", mb)
        } else {
            "$fileSizeKb KB"
        }

        // 🖼 Real image resolution using BitmapFactory
        val resolution = remember(photoPath) {
            runCatching {
                val options = android.graphics.BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                android.graphics.BitmapFactory.decodeFile(photoPath, options)
                "${options.outWidth} x ${options.outHeight}"
            }.getOrDefault("Unknown")
        }

        val context = LocalContext.current
        var readableLocation by remember { mutableStateOf("Loading...") }

        val latLong = FloatArray(2)
        val hasLocation = exif?.getLatLong(latLong) == true
        val lat = if (hasLocation) latLong[0].toDouble() else null
        val lon = if (hasLocation) latLong[1].toDouble() else null

        LaunchedEffect(photoPath) {
            if (lat != null && lon != null) {
                try {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    val address = withContext(Dispatchers.IO) {
                        geocoder.getFromLocation(lat, lon, 1)?.firstOrNull()
                    }
                    val locationString = listOfNotNull(address?.locality, address?.adminArea).joinToString(", ")
                    Log.d("PhotoLocation", "Lat: $lat, Lon: $lon -> $locationString")
                    readableLocation = locationString
                } catch (e: Exception) {
                    Log.e("PhotoLocation", "Reverse geocoding failed", e)
                    readableLocation = "Location Unavailable"
                }
            } else {
                readableLocation = "Location Unavailable"
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Text("📅 Date Taken: $dateTaken")
            Text("📁 File Size: $fileSize")
            Text("📐 Resolution: $resolution")
            Text("📍 Location: $readableLocation")
        }
    }
}
