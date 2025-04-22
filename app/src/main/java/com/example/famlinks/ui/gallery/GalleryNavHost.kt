// File: ui/gallery/GalleryNavHost.kt
package com.example.famlinks.ui.gallery

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.famlinks.viewmodel.GalleryViewModel
import com.example.famlinks.viewmodel.PhotoViewerViewModel
import com.example.famlinks.presentation.viewer.PhotoViewerScreen
import com.example.famlinks.ui.viewer.PhotoViewerUiState
import com.example.famlinks.viewmodel.PendingUploadsViewModel

@Composable
fun GalleryNavHost(
    navController: NavHostController,
    galleryViewModel: GalleryViewModel,
    photoViewerViewModel: PhotoViewerViewModel,
    pendingUploadsViewModel: PendingUploadsViewModel,
    photoViewerUiState: PhotoViewerUiState,
    setPhotoViewerUiState: (PhotoViewerUiState) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()

    // Close viewer if returning to dashboard
    LaunchedEffect(currentBackStackEntry?.destination?.route) {
        if (currentBackStackEntry?.destination?.route == "blank") {
            setPhotoViewerUiState(photoViewerUiState.copy(isVisible = false))
        }
    }

    NavHost(
        navController = navController,
        startDestination = "blank",
        modifier = modifier
    ) {
        // 🟢 ✅ This is REQUIRED or your app will crash.
        composable("blank") {
            // This is intentionally blank.
        }

        composable("allPhotos") {
            GalleryScreen(
                navController = navController,
                galleryViewModel = galleryViewModel,
                viewModel = photoViewerViewModel,
                filterType = com.example.famlinks.model.PhotoFilterType.ALL,
                onPhotoClick = { index ->
                    photoViewerViewModel.setPhotos(photoViewerViewModel.photoList.value)
                    setPhotoViewerUiState(
                        PhotoViewerUiState(isVisible = true, initialIndex = index)
                    )
                }
            )
        }

        composable("pendingUploads") {
            PendingUploadsScreen(
                navController = navController,
                pendingUploadsViewModel = pendingUploadsViewModel,
                galleryViewModel = galleryViewModel,
                modifier = Modifier.fillMaxSize()
            )
        }

        composable("moments") {
            PlaceholderGalleryScreen("Moments", modifier = Modifier.fillMaxSize())
        }

        composable("memories") {
            PlaceholderGalleryScreen("Memories", modifier = Modifier.fillMaxSize())
        }

        composable("portals") {
            PlaceholderGalleryScreen("Portals", modifier = Modifier.fillMaxSize())
        }
    }

    if (photoViewerUiState.isVisible) {
        PhotoViewerScreen(
            navController = navController,
            viewModel = photoViewerViewModel,
            initialIndex = photoViewerUiState.initialIndex,
            onClose = {
                setPhotoViewerUiState(photoViewerUiState.copy(isVisible = false))
            }
        )
    }
}



