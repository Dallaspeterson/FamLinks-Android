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
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "📸 Your Gallery",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Core categories
        GalleryNavButton("📂 Singles") { navigateTo("singles") }
        GalleryNavButton("🖼️ Albums") { navigateTo("albums") }
        GalleryNavButton("🏷️ Collections") { navigateTo("collections") }
        GalleryNavButton("🌀 Portals") { navigateTo("portals") }

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        // Utility sections
        GalleryNavButton("🗃️ All Photos") { navigateTo("allPhotos") }
        GalleryNavButton("📤 Upload Queue") { navigateTo("pendingUploads") }
    }
}

@Composable
private fun GalleryNavButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(label)
    }
}

