// File: WelcomeScreen.kt
package com.example.famlinks.ui.auth

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.famlinks.data.local.GuestManager

@Composable
fun WelcomeScreen(
    onContinueAsGuest: () -> Unit,
    onSignUpClick: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Welcome to FamLinks", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            "Capture and share your most meaningful memories with family and friends.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onSignUpClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Get Started")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Continue as Guest",
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .clickable {
                    val guestManager = GuestManager(context)
                    if (!guestManager.isGuest()) {
                        val guestId = guestManager.generateAndSaveGuestUUID()
                        Log.i("GuestSetup", "Generated guest ID: $guestId")
                    } else {
                        Log.i("GuestSetup", "Guest already exists: ${guestManager.getGuestUUID()}")
                    }
                    onContinueAsGuest()
                }
                .padding(8.dp)
        )
    }
}