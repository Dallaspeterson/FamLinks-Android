// ui/gallery/GalleryDashboardScreen.kt
package com.example.famlinks.ui.gallery

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.famlinks.ui.navigation.navigateWithSlide

@Composable
fun GalleryDashboardScreen(navController: NavController) {
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "📸 Your Photo Hub",
            style = MaterialTheme.typography.headlineMedium
        )

        Button(onClick = { navController.navigateWithSlide("moments", coroutineScope) }) {
            Text("📷 Moments")
        }

        Button(onClick = { navController.navigateWithSlide("memories", coroutineScope) }) {
            Text("🖼️ Memories")
        }

        Button(onClick = { navController.navigateWithSlide("portals", coroutineScope) }) {
            Text("🌀 Portals")
        }

        Button(onClick = { navController.navigateWithSlide("allPhotos", coroutineScope) }) {
            Text("📂 All Photos")
        }

        Button(onClick = { navController.navigateWithSlide("pendingUploads", coroutineScope) }) {
            Text("📤 Upload Queue")
        }
    }
}
