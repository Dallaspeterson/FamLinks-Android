// File: ui/gallery/GalleryDashboardScreen.kt
package com.example.famlinks.ui.gallery

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun GalleryDashboardScreen(
    navigateTo: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "📸 Your Photo Hub",
            style = MaterialTheme.typography.headlineMedium
        )

        Button(onClick = { navigateTo("moments") }) {
            Text("📷 Moments")
        }

        Button(onClick = { navigateTo("memories") }) {
            Text("🖼️ Memories")
        }

        Button(onClick = { navigateTo("portals") }) {
            Text("🌀 Portals")
        }

        Button(onClick = { navigateTo("allPhotos") }) {
            Text("📂 All Photos")
        }

        Button(onClick = { navigateTo("pendingUploads") }) {
            Text("📤 Upload Queue")
        }
    }
}
