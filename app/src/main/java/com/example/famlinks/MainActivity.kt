// File: MainActivity.kt
package com.example.famlinks

import androidx.compose.ui.Modifier
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.famlinks.data.local.GuestManager
import com.example.famlinks.ui.camera.CameraScreen
import com.example.famlinks.ui.gallery.GalleryScreen
import com.example.famlinks.ui.fam.FamScreen
import com.example.famlinks.ui.famlinks.FamLinksScreen
import com.example.famlinks.ui.portals.PortalsScreen
import com.example.famlinks.ui.auth.WelcomeScreen
import com.example.famlinks.ui.auth.SignUpScreen
import com.example.famlinks.ui.theme.FamLinksTheme
import com.example.famlinks.util.GuestCredentialsProvider
import com.example.famlinks.util.AppPreferences
import com.example.famlinks.data.remote.s3.AwsS3Client

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 0)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ), 1)
        }

        setContent {
            FamLinksTheme {
                val context = this
                val showWelcome = remember { mutableStateOf(!AppPreferences.isGuestSelected(context)) }
                var showSignUp by remember { mutableStateOf(false) }
                var selectedTab by remember { mutableStateOf(2) }
                var initialized by remember { mutableStateOf(false) }
                var triggerInit by remember { mutableStateOf(false) }
                val guestManager = remember { GuestManager(context) }

                // Handle guest creation & S3 setup

                LaunchedEffect(!showWelcome.value || triggerInit) {
                    if (!initialized && guestManager.isGuest()) {
                        GuestCredentialsProvider.getCredentialsProvider(context) // suspend call
                        AwsS3Client.initialize(context.applicationContext) // ✅ this initializes the S3 client
                        initialized = true
                    }
                }

                when {
                    showWelcome.value -> {
                        WelcomeScreen(
                            onSignUpClick = {
                                showWelcome.value = false
                                showSignUp = true
                            },
                            onContinueAsGuest = {
                                guestManager.generateAndSaveGuestUUID() // ✅ Generate the ID first
                                AppPreferences.markGuestSelected(context)
                                showWelcome.value = false
                                triggerInit = true
                            }
                        )
                    }
                    showSignUp -> {
                        SignUpScreen(onSignUpComplete = { showSignUp = false })
                    }
                    else -> {
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
                                    NavigationBarItem(
                                        selected = selectedTab == 0,
                                        onClick = { selectedTab = 0 },
                                        icon = { Icon(Icons.Default.Image, contentDescription = "Gallery") },
                                        label = { Text("Gallery") }
                                    )
                                    NavigationBarItem(
                                        selected = selectedTab == 1,
                                        onClick = { selectedTab = 1 },
                                        icon = { Icon(Icons.Default.Inbox, contentDescription = "FamLinks") },
                                        label = { Text("FamLinks") }
                                    )
                                    NavigationBarItem(
                                        selected = selectedTab == 2,
                                        onClick = { selectedTab = 2 },
                                        icon = { Icon(Icons.Default.CameraAlt, contentDescription = "Camera") },
                                        label = { Text("Camera") }
                                    )
                                    NavigationBarItem(
                                        selected = selectedTab == 3,
                                        onClick = { selectedTab = 3 },
                                        icon = { Icon(Icons.Default.Group, contentDescription = "Fam") },
                                        label = { Text("Fam") }
                                    )
                                    NavigationBarItem(
                                        selected = selectedTab == 4,
                                        onClick = { selectedTab = 4 },
                                        icon = { Icon(Icons.Default.Event, contentDescription = "Portals") },
                                        label = { Text("Portals") }
                                    )
                                }
                            }
                        ) { padding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(padding)
                            ) {
                                when (selectedTab) {
                                    0 -> GalleryScreen()
                                    1 -> FamLinksScreen()
                                    2 -> CameraScreen()
                                    3 -> FamScreen()
                                    4 -> PortalsScreen()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
