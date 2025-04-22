// File: ui/navigation/navigateWithSlide.kt
package com.example.famlinks.ui.navigation

import android.util.Log
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull

suspend fun NavHostController.navigateWithSlide(
    route: String,
    timeoutMillis: Long = 1000L // 1 second max wait
) {
    // Wait until graph is set before navigating
    val graphReady = withTimeoutOrNull(timeoutMillis) {
        while (this@navigateWithSlide.graph.startDestinationRoute == null) {
            delay(50)
        }
        true
    }

    if (graphReady == true) {
        Log.d("NavigateWithSlide", "✅ Navigating to $route")
        this.navigate(route)
    } else {
        Log.e("NavigateWithSlide", "❌ Nav graph not ready, can't navigate to $route")
    }
}
