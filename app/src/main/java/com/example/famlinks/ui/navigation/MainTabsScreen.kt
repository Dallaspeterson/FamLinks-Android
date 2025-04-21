// File: ui/navigation/MainTabsScreen.kt
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
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.famlinks.viewmodel.GalleryViewModel
import com.example.famlinks.viewmodel.PhotoViewerViewModel
import com.example.famlinks.ui.fam.FamScreen
import com.example.famlinks.ui.famlinks.FamLinksScreen
import com.example.famlinks.ui.gallery.GalleryNavHost
import com.example.famlinks.ui.portals.PortalsScreen
import com.example.famlinks.ui.camera.CameraScreen
import com.example.famlinks.viewmodel.PendingUploadsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTabsScreen() {
    val galleryNavController = rememberNavController()
    val cameraNavController = rememberNavController()
    val context = LocalContext.current
    val galleryViewModel: GalleryViewModel = viewModel()
    val photoViewerViewModel: PhotoViewerViewModel = viewModel()
    val pendingUploadsViewModel: PendingUploadsViewModel = viewModel()

    val tabs = listOf(
        TabScreen("gallery", "Gallery", { Icon(Icons.Default.Collections, contentDescription = null) }),
        TabScreen("famlinks", "FamLinks", { Icon(Icons.Default.Inbox, contentDescription = null) }),
        TabScreen("camera", "Camera", { Icon(Icons.Default.CameraAlt, contentDescription = null) }),
        TabScreen("fam", "Fam", { Icon(Icons.Default.Group, contentDescription = null) }),
        TabScreen("portals", "Portals", { Icon(Icons.Default.Event, contentDescription = null) })
    )

    var currentTab by remember { mutableStateOf("camera") }


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
                            if (currentTab == screen.route) {
                                if (screen.route == "gallery") {
                                    galleryNavController.navigate("galleryHub") {
                                        popUpTo("galleryHub") { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            } else {
                                currentTab = screen.route
                            }
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
                "gallery" -> GalleryNavHost(
                    navController = galleryNavController,
                    galleryViewModel = galleryViewModel,
                    photoViewerViewModel = photoViewerViewModel,
                    pendingUploadsViewModel = pendingUploadsViewModel
                )
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

data class TabScreen(val route: String, val label: String, val icon: @Composable () -> Unit)