// File: MainActivity.kt
package com.example.famlinks

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.famlinks.data.local.GuestManager
import com.example.famlinks.ui.navigation.MainTabsScreen
import com.example.famlinks.ui.auth.SignUpScreen
import com.example.famlinks.ui.auth.WelcomeScreen
import com.example.famlinks.ui.theme.FamLinksTheme
import com.example.famlinks.util.AppPreferences
import com.example.famlinks.util.GuestCredentialsProvider
import com.example.famlinks.data.remote.s3.AwsS3Client

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request camera and location permissions
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
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                1
            )
        }

        setContent {
            FamLinksTheme {
                val context = this
                val showWelcome = remember { mutableStateOf(!AppPreferences.isGuestSelected(context)) }
                var showSignUp by remember { mutableStateOf(false) }
                var initialized by remember { mutableStateOf(false) }
                var triggerInit by remember { mutableStateOf(false) }
                val guestManager = remember { GuestManager(context) }

                // Handle guest account setup and S3 init
                LaunchedEffect(!showWelcome.value || triggerInit) {
                    if (!initialized && guestManager.isGuest()) {
                        GuestCredentialsProvider.getCredentialsProvider(context)
                        AwsS3Client.initialize(context.applicationContext)
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
                                guestManager.generateAndSaveGuestUUID()
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
                        MainTabsScreen()
                    }
                }
            }
        }
    }
}
