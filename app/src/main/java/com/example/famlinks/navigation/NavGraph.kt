// File: navigation/NavGraph.kt
package com.example.famlinks.navigation

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.famlinks.ui.auth.SignUpScreen
import com.example.famlinks.ui.auth.WelcomeScreen
import com.example.famlinks.ui.camera.CameraScreen
import com.example.famlinks.ui.gallery.GalleryScreen
import com.example.famlinks.ui.viewer.PhotoViewerScreen
import com.example.famlinks.util.AppPreferences
import java.io.File

@SuppressLint("RestrictedApi")
@Composable
fun FamLinksApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val context = LocalContext.current

    val startDestination = if (AppPreferences.isGuestSelected(context)) {
        "camera"
    } else {
        "welcome"
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable("welcome") {
            WelcomeScreen(
                onSignUpClick = { navController.navigate("signup") },
                onContinueAsGuest = {
                    AppPreferences.markGuestSelected(context)
                    navController.navigate("camera") {
                        popUpTo("welcome") { inclusive = true }
                    }
                }
            )
        }

        composable("signup") {
            SignUpScreen(onSignUpComplete = {
                navController.navigate("camera") {
                    popUpTo("signup") { inclusive = true }
                }
            })
        }

        composable("camera") {
            CameraScreen()
        }

        composable("gallery") {
            GalleryScreen()
        }

        composable(
            route = "photoViewer/{initialIndex}",
            arguments = listOf(navArgument("initialIndex") { type = NavType.IntType })
        ) { backStackEntry ->
            val index = backStackEntry.arguments?.getInt("initialIndex") ?: 0
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
