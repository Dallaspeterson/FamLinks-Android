// File: ui/gallery/GalleryScreen.kt
package com.example.famlinks.ui.gallery

import android.widget.Toast
import android.os.Environment
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen() {
    val context = LocalContext.current
    val mediaDir = context.getExternalFilesDir("FamLinks")?.apply { mkdirs() }
    var imageFiles by remember {
        mutableStateOf(
            mediaDir?.listFiles()?.sortedByDescending { it.lastModified() } ?: emptyList()
        )
    }
    var selectMode by remember { mutableStateOf(false) }
    var selectedPhotos by remember { mutableStateOf(setOf<File>()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (selectMode) "Select Photos" else "Gallery") },
                navigationIcon = {
                    IconButton(onClick = {
                        selectMode = false
                        selectedPhotos = emptySet()
                    }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!selectMode) {
                        TextButton(onClick = { selectMode = true }) {
                            Text("Select")
                        }
                    } else {
                        TextButton(onClick = {
                            selectMode = false
                            selectedPhotos = emptySet()
                        }) {
                            Text("Cancel")
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (selectMode) {
                BottomAppBar {
                    Spacer(Modifier.weight(1f))
                    IconButton(
                        onClick = {
                            selectedPhotos.forEach { it.delete() }
                            imageFiles = mediaDir?.listFiles()?.sortedByDescending { it.lastModified() } ?: emptyList()
                            selectedPhotos = emptySet()
                        },
                        enabled = selectedPhotos.isNotEmpty()
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete")
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 120.dp),
            modifier = Modifier
                .padding(paddingValues)
                .padding(8.dp),
            contentPadding = PaddingValues(4.dp)
        ) {
            items(imageFiles) { file ->
                val uri = file.toUri()
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .padding(4.dp)
                        .clickable {
                            if (selectMode) {
                                selectedPhotos = if (selectedPhotos.contains(file)) {
                                    selectedPhotos - file
                                } else {
                                    selectedPhotos + file
                                }
                            } else {
                                // TODO: implement photo viewing later
                                Toast.makeText(context, "Photo tapped (viewer coming soon)", Toast.LENGTH_SHORT).show()
                            }
                        }
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(uri),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                    if (selectMode && selectedPhotos.contains(file)) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(6.dp)
                                .size(24.dp)
                                .background(Color(0xFF4CAF50), shape = MaterialTheme.shapes.small),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✓", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
