// File: ui/gallery/GalleryNavHost.kt
// File: ui/gallery/GalleryNavHost.kt
package com.example.famlinks.ui.gallery

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.famlinks.viewmodel.GalleryViewModel
import com.example.famlinks.viewmodel.PhotoViewerViewModel
import com.example.famlinks.presentation.viewer.PhotoViewerScreen
import com.example.famlinks.viewmodel.PendingUploadsViewModel

@Composable
fun GalleryNavHost(
    navController: NavHostController,
    galleryViewModel: GalleryViewModel,
    photoViewerViewModel: PhotoViewerViewModel,
    pendingUploadsViewModel: PendingUploadsViewModel, // ✅ new
    modifier: Modifier = Modifier,
) {
    var showPhotoViewer by remember { mutableStateOf(false) }
    var initialPhotoIndex by remember { mutableStateOf(0) }

    NavHost(
        navController = navController,
        startDestination = "galleryHub",
        modifier = modifier
    ) {
        composable("galleryHub") {
            GalleryDashboardScreen(navController)
        }

        composable("moments") {
            PlaceholderGalleryScreen("Moments")
        }

        composable("memories") {
            PlaceholderGalleryScreen("Memories")
        }

        composable("portals") {
            PlaceholderGalleryScreen("Portals")
        }

        composable("allPhotos") {
            GalleryScreen(
                navController = navController,
                galleryViewModel = galleryViewModel,
                viewModel = photoViewerViewModel,
                filterType = com.example.famlinks.model.PhotoFilterType.ALL,
                onPhotoClick = {
                    initialPhotoIndex = it
                    showPhotoViewer = true
                }
            )
        }

        composable("pendingUploads") {
            PendingUploadsScreen(
                navController = navController,
                pendingUploadsViewModel = pendingUploadsViewModel,
                galleryViewModel = galleryViewModel
            )
        }
    }

    AnimatedVisibility(
        visible = showPhotoViewer,
        enter = slideInHorizontally(initialOffsetX = { it }),
        exit = slideOutHorizontally(targetOffsetX = { it })
    ) {
        PhotoViewerScreen(
            navController = navController,
            viewModel = photoViewerViewModel,
            initialIndex = initialPhotoIndex,
            onClose = { showPhotoViewer = false }
        )
    }
}
