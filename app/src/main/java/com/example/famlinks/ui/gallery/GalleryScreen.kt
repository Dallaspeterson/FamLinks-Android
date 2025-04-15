// File: ui/gallery/GalleryScreen.kt
package com.example.famlinks.ui.gallery

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.famlinks.data.remote.s3.S3GalleryLoader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen() {
    val context = LocalContext.current
    var s3ImageUrls by remember { mutableStateOf(emptyList<String>()) }

    LaunchedEffect(Unit) {
        s3ImageUrls = S3GalleryLoader.listPhotoUrls()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Gallery") }) }
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 120.dp),
            modifier = Modifier
                .padding(paddingValues)
                .padding(8.dp),
            contentPadding = PaddingValues(4.dp)
        ) {
            items(s3ImageUrls) { url ->
                Image(
                    painter = rememberAsyncImagePainter(url),
                    contentDescription = null,
                    modifier = Modifier
                        .aspectRatio(1f)
                        .padding(4.dp)
                        .clickable {
                            Toast.makeText(context, "Tapped: $url", Toast.LENGTH_SHORT).show()
                        }
                )
            }
        }
    }
}
