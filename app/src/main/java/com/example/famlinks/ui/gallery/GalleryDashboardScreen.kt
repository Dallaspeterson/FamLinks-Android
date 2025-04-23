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
            text = "📸 Your Gallery",
            style = MaterialTheme.typography.headlineMedium
        )

        Button(onClick = { navigateTo("singles") }) {
            Text("📂 Singles")
        }

        Button(onClick = { navigateTo("albums") }) {
            Text("🖼️ Albums")
        }

        Button(onClick = { navigateTo("collections") }) {
            Text("🏷️ Collections")
        }

        Button(onClick = { navigateTo("portals") }) {
            Text("🌀 Portals")
        }

        Divider()

        Button(onClick = { navigateTo("allPhotos") }) {
            Text("🗃️ All Photos")
        }

        Button(onClick = { navigateTo("pendingUploads") }) {
            Text("📤 Upload Queue")
        }
    }
}
