// File: ui/navigation/MainTabsScreen.kt
package com.example.famlinks.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.famlinks.ui.camera.CameraScreen
import com.example.famlinks.ui.gallery.GalleryScreen
import com.example.famlinks.ui.fam.FamScreen
import com.example.famlinks.ui.famlinks.FamLinksScreen
import com.example.famlinks.ui.portals.PortalsScreen
import com.example.famlinks.presentation.viewer.PhotoViewerScreen
import com.example.famlinks.viewmodel.GalleryViewModel
import com.example.famlinks.viewmodel.PhotoViewerViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTabsScreen() {
    val context = LocalContext.current
    val photoViewerViewModel: PhotoViewerViewModel = viewModel()
    val galleryViewModel: GalleryViewModel = viewModel()
    val navController = rememberNavController()

    val tabs = listOf(
        TabScreen("gallery", "Gallery", { Icon(Icons.Default.Image, contentDescription = "Gallery") }),
        TabScreen("famlinks", "FamLinks", { Icon(Icons.Default.Inbox, contentDescription = "FamLinks") }),
        TabScreen("camera", "Camera", { Icon(Icons.Default.CameraAlt, contentDescription = "Camera") }),
        TabScreen("fam", "Fam", { Icon(Icons.Default.Group, contentDescription = "Fam") }),
        TabScreen("portals", "Portals", { Icon(Icons.Default.Event, contentDescription = "Portals") }),
    )

    var currentTab by remember { mutableStateOf("camera") }
    var showPhotoViewer by remember { mutableStateOf(false) }
    var initialPhotoIndex by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FamLinks") },
                actions = {
                    IconButton(onClick = { /* TODO: Profile */ }) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEach { screen ->
                    NavigationBarItem(
                        selected = currentTab == screen.route,
                        onClick = {
                            currentTab = screen.route
                            showPhotoViewer = false // closes viewer when switching tabs
                        },
                        icon = screen.icon,
                        label = { Text(screen.label) }
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {

            when (currentTab) {
                "gallery" -> GalleryScreen(
                    navController = navController,
                    viewModel = photoViewerViewModel,
                    galleryViewModel = galleryViewModel,
                    onPhotoClick = { index ->
                        initialPhotoIndex = index
                        showPhotoViewer = true
                    }
                )
                "camera" -> CameraScreen(
                    galleryViewModel = galleryViewModel,
                    navController = navController
                )
                "famlinks" -> FamLinksScreen()
                "fam" -> FamScreen()
                "portals" -> PortalsScreen()
            }

            AnimatedVisibility(
                visible = showPhotoViewer,
                enter = slideInHorizontally(initialOffsetX = { it }),
                exit = slideOutHorizontally(targetOffsetX = { it })
            ) {
                PhotoViewerScreen(
                    navController = navController,
                    initialIndex = initialPhotoIndex,
                    viewModel = photoViewerViewModel,
                    onClose = { showPhotoViewer = false }
                )
            }
        }
    }
}

data class TabScreen(val route: String, val label: String, val icon: @Composable () -> Unit)
