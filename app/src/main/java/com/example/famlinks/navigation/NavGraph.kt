// File: navigation/NavGraph.kt
package com.example.famlinks.navigation

import com.example.famlinks.ui.camera.CameraScreen
import com.example.famlinks.ui.gallery.GalleryScreen
import com.example.famlinks.ui.viewer.PhotoViewerScreen
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import androidx.compose.ui.platform.LocalContext
import java.io.File

@Composable
fun FamLinksApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "camera"
    ) {
        composable("camera") {
            CameraScreen() // ✅ No navController
        }

        composable("gallery") {
            GalleryScreen() // ✅ No navController
        }

        // ✅ PhotoViewer still uses navController
        composable(
            route = "photoViewer/{initialIndex}",
            arguments = listOf(navArgument("initialIndex") { type = NavType.IntType })
        ) { backStackEntry ->
            val index = backStackEntry.arguments?.getInt("initialIndex") ?: 0

            val context = LocalContext.current
            val mediaDir = context.getExternalFilesDir("FamLinks")?.apply { mkdirs() }
            val imageFiles = mediaDir?.listFiles()?.sortedByDescending { it.lastModified() } ?: emptyList()
            val photoPaths = imageFiles.map { it.absolutePath }

            PhotoViewerScreen(
                navController = navController,
                photoPaths = photoPaths,
                initialIndex = index
            )
        }
    }
}
