// File: MainActivity.kt
package com.example.famlinks

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.core.Amplify
import com.example.famlinks.ui.camera.CameraScreen
import com.example.famlinks.ui.gallery.GalleryScreen
import com.example.famlinks.ui.fam.FamScreen
import com.example.famlinks.ui.famlinks.FamLinksScreen
import com.example.famlinks.ui.portals.PortalsScreen
import com.example.famlinks.ui.auth.WelcomeScreen
import com.example.famlinks.ui.auth.SignUpScreen
import com.example.famlinks.ui.theme.FamLinksTheme
import com.example.famlinks.data.local.GuestManager

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // AWS Amplify Initialization
        try {
            Amplify.addPlugin(AWSCognitoAuthPlugin())
            Amplify.configure(applicationContext)
            Log.i("FamLinks", "Amplify initialized successfully")
        } catch (error: Exception) {
            Log.e("FamLinks", "Failed to initialize Amplify", error)
        }

        // Permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 0)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                1
            )
        }

        // UI
        setContent {
            FamLinksTheme {
                val context = this
                val guestManager = GuestManager(context)
                val isUserSignedIn = try {
                    Amplify.Auth.currentUser != null
                } catch (e: Exception) {
                    false
                }

                var showWelcome by remember {
                    mutableStateOf(!isUserSignedIn && !guestManager.isGuest())
                }

                var showSignUp by remember { mutableStateOf(false) }
                var selectedTab by remember { mutableStateOf(2) } // Start on Camera

                when {
                    showWelcome -> {
                        WelcomeScreen(
                            onSignUpClick = {
                                showWelcome = false
                                showSignUp = true
                            },
                            onContinueAsGuest = {
                                guestManager.generateAndSaveGuestUUID() // Safe to call again, does nothing if already generated
                                showWelcome = false
                            }
                        )
                    }
                    showSignUp -> {
                        SignUpScreen(
                            onSignUpComplete = {
                                showSignUp = false
                            }
                        )
                    }
                    else -> {
                        Scaffold(
                            topBar = {
                                TopAppBar(
                                    title = { Text("FamLinks") },
                                    actions = {
                                        IconButton(onClick = { /* Profile settings */ }) {
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
                            Box(modifier = Modifier
                                .fillMaxSize()
                                .padding(padding)) {
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
