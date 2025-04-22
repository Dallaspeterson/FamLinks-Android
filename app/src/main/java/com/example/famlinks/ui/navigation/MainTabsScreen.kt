// File: ui/navigation/MainTabsScreen.kt
package com.example.famlinks.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.famlinks.presentation.viewer.PhotoViewerScreen
import com.example.famlinks.ui.camera.CameraScreen
import com.example.famlinks.ui.fam.FamScreen
import com.example.famlinks.ui.famlinks.FamLinksScreen
import com.example.famlinks.ui.gallery.GalleryDashboardScreen
import com.example.famlinks.ui.gallery.GalleryNavHost
import com.example.famlinks.ui.portals.PortalsScreen
import com.example.famlinks.ui.viewer.PhotoViewerUiState
import com.example.famlinks.viewmodel.GalleryViewModel
import com.example.famlinks.viewmodel.PendingUploadsViewModel
import com.example.famlinks.viewmodel.PhotoViewerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTabsScreen() {
    val galleryNavController = rememberNavController()
    val cameraNavController = rememberNavController()
    val context = LocalContext.current
    val galleryViewModel: GalleryViewModel = viewModel()
    val photoViewerViewModel: PhotoViewerViewModel = viewModel()
    val pendingUploadsViewModel: PendingUploadsViewModel = viewModel()

    var currentTab by remember { mutableStateOf("camera") }
    var photoViewerUiState by remember { mutableStateOf(PhotoViewerUiState()) }
    var lastGalleryRoute by remember { mutableStateOf("blank") }
    var galleryScreenState by remember { mutableStateOf("dashboard") }
    var pendingGalleryNavigation by remember { mutableStateOf<String?>(null) }

    val tabs = listOf(
        TabScreen("gallery", "Gallery") { Icon(Icons.Default.Collections, null) },
        TabScreen("famlinks", "FamLinks") { Icon(Icons.Default.Inbox, null) },
        TabScreen("camera", "Camera") { Icon(Icons.Default.CameraAlt, null) },
        TabScreen("fam", "Fam") { Icon(Icons.Default.Group, null) },
        TabScreen("portals", "Portals") { Icon(Icons.Default.Event, null) }
    )

    val currentGalleryRoute by galleryNavController.currentBackStackEntryAsState()
    val currentGalleryRouteId = currentGalleryRoute?.destination?.route

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FamLinks") },
                actions = {
                    IconButton(onClick = { /* TODO: profile settings */ }) {
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
                            if (currentTab == screen.route && screen.route == "gallery") {
                                if (photoViewerUiState.isVisible) {
                                    photoViewerUiState = photoViewerUiState.copy(isVisible = false)
                                    pendingGalleryNavigation = "blank"
                                    galleryScreenState = "dashboard"
                                } else if (currentGalleryRouteId != "blank") {
                                    pendingGalleryNavigation = "blank"
                                    galleryScreenState = "dashboard"
                                }
                            } else {
                                if (screen.route == "gallery") {
                                    currentTab = "gallery"
                                    val target = if (lastGalleryRoute != "blank") lastGalleryRoute else "blank"
                                    pendingGalleryNavigation = target
                                    galleryScreenState = if (target == "blank") "dashboard" else "navhost"
                                } else {
                                    currentTab = screen.route
                                }
                            }

                            if (screen.route != "gallery" &&
                                currentGalleryRouteId != null &&
                                currentGalleryRouteId != "blank"
                            ) {
                                lastGalleryRoute = currentGalleryRouteId
                            }
                        },
                        icon = screen.icon,
                        label = { Text(screen.label) }
                    )
                }
            }
        }
    ) { padding ->
        // Ensure navigation only happens after graph is ready
        LaunchedEffect(pendingGalleryNavigation, galleryScreenState) {
            if (galleryScreenState == "navhost" && pendingGalleryNavigation != null) {
                galleryNavController.navigate(pendingGalleryNavigation!!) {
                    launchSingleTop = true
                    popUpTo("blank") { inclusive = (pendingGalleryNavigation == "blank") }
                }
                pendingGalleryNavigation = null
            }
        }

        Box(modifier = Modifier.padding(padding)) {
            when (currentTab) {
                "gallery" -> {
                    when {
                        photoViewerUiState.isVisible -> {
                            PhotoViewerScreen(
                                navController = galleryNavController,
                                viewModel = photoViewerViewModel,
                                initialIndex = photoViewerUiState.initialIndex,
                                onClose = {
                                    photoViewerUiState = photoViewerUiState.copy(isVisible = false)
                                }
                            )
                        }

                        galleryScreenState == "dashboard" -> {
                            GalleryDashboardScreen(
                                navigateTo = { route ->
                                    pendingGalleryNavigation = route
                                    galleryScreenState = "navhost"
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        else -> {
                            GalleryNavHost(
                                navController = galleryNavController,
                                galleryViewModel = galleryViewModel,
                                photoViewerViewModel = photoViewerViewModel,
                                pendingUploadsViewModel = pendingUploadsViewModel,
                                photoViewerUiState = photoViewerUiState,
                                setPhotoViewerUiState = { photoViewerUiState = it }
                            )
                        }
                    }
                }

                "camera" -> CameraScreen(
                    galleryViewModel = galleryViewModel,
                    navController = cameraNavController
                )

                "famlinks" -> FamLinksScreen()
                "fam" -> FamScreen()
                "portals" -> PortalsScreen()
            }
        }
    }
}

data class TabScreen(
    val route: String,
    val label: String,
    val icon: @Composable () -> Unit
)


