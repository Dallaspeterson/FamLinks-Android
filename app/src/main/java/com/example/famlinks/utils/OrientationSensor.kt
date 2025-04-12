// File: utils/OrientationSensor.kt
package com.example.famlinks.utils


import android.content.Context
import android.view.OrientationEventListener
import androidx.compose.runtime.*

enum class DeviceOrientation {
    Portrait, Landscape
}

@Composable
fun rememberDeviceOrientation(context: Context): DeviceOrientation {
    val orientation = remember { mutableStateOf(DeviceOrientation.Portrait) }

    DisposableEffect(context) {
        val listener = object : OrientationEventListener(context) {
            override fun onOrientationChanged(angle: Int) {
                orientation.value = if (angle in 45..134 || angle in 225..314) {
                    DeviceOrientation.Landscape
                } else {
                    DeviceOrientation.Portrait
                }
            }
        }
        listener.enable()
        onDispose { listener.disable() }
    }
    return orientation.value
}