// File: navigation/NavGraph.kt
package com.example.famlinks.navigation

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.famlinks.ui.auth.SignUpScreen
import com.example.famlinks.ui.auth.WelcomeScreen
import com.example.famlinks.ui.camera.CameraScreen
import com.example.famlinks.ui.gallery.GalleryScreen
import com.example.famlinks.ui.fam.FamScreen
import com.example.famlinks.ui.famlinks.FamLinksScreen
import com.example.famlinks.ui.portals.PortalsScreen
import com.example.famlinks.util.AppPreferences
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.famlinks.presentation.viewer.PhotoViewerScreen
import com.example.famlinks.viewmodel.PhotoViewerViewModel

data class Screen(val route: String, val icon: ImageVector, val label: String)

@SuppressLint("RestrictedApi")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamLinksApp(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val navController = rememberNavController()

    val tabs = listOf(
        Screen("gallery", Icons.Default.Image, "Gallery"),
        Screen("famlinks", Icons.Default.Inbox, "FamLinks"),
        Screen("camera", Icons.Default.CameraAlt, "Camera"),
        Screen("fam", Icons.Default.Group, "Fam"),
        Screen("portals", Icons.Default.Event, "Portals")
    )

    val startDestination = if (AppPreferences.isGuestSelected(context)) "camera" else "welcome"
    var currentRoute by remember { mutableStateOf(startDestination) }
    val photoViewerViewModel: PhotoViewerViewModel = viewModel()

    Scaffold(
        topBar = {
            if (!currentRoute.startsWith("photoViewer") && currentRoute != "welcome" && currentRoute != "signup") {
                TopAppBar(
                    title = { Text("FamLinks") },
                    actions = {
                        IconButton(onClick = { /* TODO: Add profile logic */ }) {
                            Icon(Icons.Default.Person, contentDescription = "Profile")
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (!currentRoute.startsWith("photoViewer") && currentRoute != "welcome" && currentRoute != "signup") {
                NavigationBar {
                    tabs.forEach { screen ->
                        NavigationBarItem(
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo("camera") { inclusive = false }
                                    launchSingleTop = true
                                }
                            },
                            icon = { Icon(screen.icon, contentDescription = screen.label) },
                            label = { Text(screen.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("welcome") {
                currentRoute = "welcome"
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
                currentRoute = "signup"
                SignUpScreen(onSignUpComplete = {
                    navController.navigate("camera") {
                        popUpTo("signup") { inclusive = true }
                    }
                })
            }

            composable("camera") {
                currentRoute = "camera"
                CameraScreen()
            }

            composable("gallery") {
                currentRoute = "gallery"
                GalleryScreen(
                    navController = navController,
                    viewModel = photoViewerViewModel
                )
            }

            composable("famlinks") {
                currentRoute = "famlinks"
                FamLinksScreen()
            }

            composable("fam") {
                currentRoute = "fam"
                FamScreen()
            }

            composable("portals") {
                currentRoute = "portals"
                PortalsScreen()
            }

            composable(
                route = "photoViewer/{initialIndex}",
                arguments = listOf(navArgument("initialIndex") { type = NavType.IntType })
            ) { backStackEntry ->
                currentRoute = "photoViewer"
                val index = backStackEntry.arguments?.getInt("initialIndex") ?: 0

                PhotoViewerScreen(
                    navController = navController,
                    initialIndex = index,
                    viewModel = photoViewerViewModel
                )
            }
        }
    }
}

