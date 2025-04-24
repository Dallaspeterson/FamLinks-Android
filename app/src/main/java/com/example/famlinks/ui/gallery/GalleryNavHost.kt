// File: ui/gallery/GalleryNavHost.kt
package com.example.famlinks.ui.gallery

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.famlinks.model.PhotoFilterType
import com.example.famlinks.presentation.viewer.PhotoViewerScreen
import com.example.famlinks.ui.viewer.PhotoViewerUiState
import com.example.famlinks.viewmodel.GalleryViewModel
import com.example.famlinks.viewmodel.PendingUploadsViewModel
import com.example.famlinks.viewmodel.PhotoViewerViewModel

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
        composable("blank") {}

        composable("allPhotos") {
            DynamicGalleryScreen(
                navController = navController,
                galleryViewModel = galleryViewModel,
                photoViewerViewModel = photoViewerViewModel,
                filterType = PhotoFilterType.ALL,
                onPhotoClick = { index ->
                    val list = photoViewerViewModel.photoList.value
                    photoViewerViewModel.setPhotos(list)
                    setPhotoViewerUiState(
                        PhotoViewerUiState(
                            isVisible = true,
                            photoList = list,
                            initialIndex = index
                        )
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

        composable("singles") {
            DynamicGalleryScreen(
                navController = navController,
                galleryViewModel = galleryViewModel,
                photoViewerViewModel = photoViewerViewModel,
                filterType = PhotoFilterType.SINGLES,
                onPhotoClick = { index ->
                    val list = photoViewerViewModel.photoList.value
                    photoViewerViewModel.setPhotos(list)
                    setPhotoViewerUiState(
                        PhotoViewerUiState(
                            isVisible = true,
                            photoList = list,
                            initialIndex = index
                        )
                    )
                }
            )
        }

        composable("albums") {
            DynamicGalleryScreen(
                navController = navController,
                galleryViewModel = galleryViewModel,
                photoViewerViewModel = photoViewerViewModel,
                filterType = PhotoFilterType.ALBUMS,
                onPhotoClick = { index ->
                    val list = photoViewerViewModel.photoList.value
                    photoViewerViewModel.setPhotos(list)
                    setPhotoViewerUiState(
                        PhotoViewerUiState(
                            isVisible = true,
                            photoList = list,
                            initialIndex = index
                        )
                    )
                }
            )
        }

        composable("collections") {
            DynamicGalleryScreen(
                navController = navController,
                galleryViewModel = galleryViewModel,
                photoViewerViewModel = photoViewerViewModel,
                filterType = PhotoFilterType.COLLECTIONS,
                onPhotoClick = { index ->
                    val list = photoViewerViewModel.photoList.value
                    photoViewerViewModel.setPhotos(list)
                    setPhotoViewerUiState(
                        PhotoViewerUiState(
                            isVisible = true,
                            photoList = list,
                            initialIndex = index
                        )
                    )
                }
            )
        }

        composable("portals") {
            DynamicGalleryScreen(
                navController = navController,
                galleryViewModel = galleryViewModel,
                photoViewerViewModel = photoViewerViewModel,
                filterType = PhotoFilterType.PORTALS,
                onPhotoClick = { index ->
                    val list = photoViewerViewModel.photoList.value
                    photoViewerViewModel.setPhotos(list)
                    setPhotoViewerUiState(
                        PhotoViewerUiState(
                            isVisible = true,
                            photoList = list,
                            initialIndex = index
                        )
                    )
                }
            )
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






