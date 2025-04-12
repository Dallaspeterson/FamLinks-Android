// File: ui/camera/CameraPreview.kt
package com.example.famlinks.ui.camera


import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch

// Global reference to the current preview
object PreviewHolder {
    var previewView: PreviewView? = null

    fun captureBitmap(): Bitmap? {
        return previewView?.bitmap
    }
}

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    imageCapture: ImageCapture,
    cameraSelector: CameraSelector,
    setCameraControl: (CameraControl) -> Unit,
    key: String // NEW
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    val previewView = remember { PreviewView(context) }
    PreviewHolder.previewView = previewView // store for access in CameraScreen

    var camera by remember { mutableStateOf<Camera?>(null) }

    LaunchedEffect(cameraSelector, key) {
        val cameraProvider = ProcessCameraProvider.getInstance(context).get()

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        try {
            cameraProvider.unbindAll()
            camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )
            camera?.cameraControl?.let(setCameraControl)
        } catch (e: Exception) {
            Log.e("CameraPreview", "Camera binding failed", e)
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier.pointerInput(camera) {
            detectTransformGestures { _, _, zoomChange, _ ->
                camera?.let { cam ->
                    val zoomState = cam.cameraInfo.zoomState.value
                    val newZoom = (zoomState?.zoomRatio ?: 1f) * zoomChange
                    val clamped = newZoom.coerceIn(
                        zoomState?.minZoomRatio ?: 1f,
                        zoomState?.maxZoomRatio ?: 5f
                    )
                    coroutineScope.launch {
                        cam.cameraControl.setZoomRatio(clamped)
                    }
                }
            }
        }
    )
}

