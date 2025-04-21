// File: ui/navigation/navigateWithSlide.kt
package com.example.famlinks.ui.navigation

import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

fun NavController.navigateWithSlide(
    route: String,
    scope: CoroutineScope
) {
    // Avoid duplicate nav events
    if (currentDestination?.route == route) return

    // Launch animation with slight delay
    scope.launch {
        delay(100) // Optional smoothness delay
        navigate(route)
    }
}
