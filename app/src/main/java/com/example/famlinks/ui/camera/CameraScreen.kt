// File: ui/camera/CameraScreen.kt
package com.example.famlinks.ui.camera

import com.example.famlinks.util.AppPreferences
import android.graphics.Bitmap
import android.location.Location
import android.os.Build
import android.util.Log
import android.net.Uri
import android.widget.Toast
import android.content.Context
import androidx.annotation.RequiresApi
import androidx.camera.core.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.famlinks.viewmodel.CameraViewModel
import com.example.famlinks.data.remote.s3.S3Uploader
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume

@Composable
fun CameraScreen() {
    val context = LocalContext.current
    val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    val coroutineScope = rememberCoroutineScope()
    var imageCapture by remember { mutableStateOf(ImageCapture.Builder().build()) }
    val guestUUID by remember { mutableStateOf(AppPreferences.getGuestId(context) ?: "") }
    var cameraSessionKey by remember { mutableStateOf(UUID.randomUUID().toString()) }

    val cameraViewModel: CameraViewModel = viewModel()
    val lensFacing by cameraViewModel.lensFacing.collectAsState()
    val flashEnabled by cameraViewModel.flashEnabled.collectAsState()
    val lastPhotoUri by cameraViewModel.lastPhotoUri.collectAsState()

    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }

    val cameraSelector = remember(lensFacing) {
        CameraSelector.Builder().requireLensFacing(lensFacing).build()
    }

    val flashAlpha by animateFloatAsState(
        targetValue = if (flashEnabled) 0.9f else 0f,
        animationSpec = tween(100),
        label = "flashAlpha"
    )

    val isEmulator = Build.FINGERPRINT.contains("generic")

    LaunchedEffect(flashEnabled) {
        imageCapture = ImageCapture.Builder().build().apply {
            flashMode = if (flashEnabled) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF
        }
        cameraSessionKey = UUID.randomUUID().toString()
    }

    fun saveBitmapAsPhoto(bitmap: Bitmap): Uri? {
        val mediaDir = context.getExternalFilesDir("FamLinks")?.apply { mkdirs() } ?: return null
        val file = File(
            mediaDir,
            SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis()) + ".jpg"
        )
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        }
        return file.toUri()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            imageCapture = imageCapture,
            cameraSelector = cameraSelector,
            setCameraControl = { cameraControl = it },
            key = cameraSessionKey
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconButton(onClick = { cameraViewModel.toggleCamera() }) {
                Icon(Icons.Default.Cameraswitch, contentDescription = "Swap Camera", tint = Color.White)
            }
            if (!isEmulator) {
                IconButton(onClick = { cameraViewModel.toggleFlash() }) {
                    Box {
                        Icon(Icons.Default.FlashOn, contentDescription = "Toggle Flash", tint = Color.White)
                        if (!flashEnabled) {
                            Box(
                                Modifier
                                    .matchParentSize()
                                    .padding(6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Divider(
                                    thickness = 2.dp,
                                    color = Color.White,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .rotate(45f)
                                )
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
            onClick = {
                coroutineScope.launch {
                    val isFrontCamera = lensFacing == CameraSelector.LENS_FACING_FRONT
                    val currentLocation = suspendCancellableCoroutine<Location?> { continuation ->
                        fusedLocationClient.lastLocation
                            .addOnSuccessListener { continuation.resume(it) }
                            .addOnFailureListener { continuation.resume(null) }
                    }

                    if (isFrontCamera) {
                        if (flashEnabled) delay(750)
                        val bitmap = PreviewHolder.captureBitmap()
                        if (bitmap != null) {
                            val uri = saveBitmapAsPhoto(bitmap)
                            if (uri != null) cameraViewModel.setLastPhotoUri(uri)
                        } else {
                            Toast.makeText(context, "Capture failed", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        val mediaDir = context.getExternalFilesDir("FamLinks")?.apply { mkdirs() }
                        val photoFile = File(
                            mediaDir,
                            SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis()) + ".jpg"
                        )

                        val metadata = ImageCapture.Metadata().apply {
                            location = currentLocation
                        }

                        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile)
                            .setMetadata(metadata)
                            .build()

                        imageCapture.takePicture(
                            outputOptions,
                            ContextCompat.getMainExecutor(context),
                            object : ImageCapture.OnImageSavedCallback {
                                override fun onError(exc: ImageCaptureException) {
                                    Toast.makeText(context, "Capture failed. Try turning off flash.", Toast.LENGTH_SHORT).show()
                                    cameraViewModel.setFlash(false)
                                }

                                @RequiresApi(Build.VERSION_CODES.O)
                                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                    coroutineScope.launch {
                                        withContext(Dispatchers.IO) {
                                            if (!photoFile.exists()) {
                                                Log.e("CameraScreen", "❌ File not found: ${photoFile.absolutePath}")
                                                return@withContext
                                            }
                                            val success = S3Uploader.uploadPhoto(context, photoFile)
                                            Log.i("CameraScreen", "Upload success: $success")
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        ) {
            Icon(Icons.Default.CameraAlt, contentDescription = "Capture")
        }

        lastPhotoUri?.let { uri ->
            IconButton(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
                    .size(56.dp),
                onClick = {
                    // Optional: open gallery tab
                }
            ) {
                Image(
                    painter = rememberAsyncImagePainter(uri),
                    contentDescription = "Last photo",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        if (flashAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .alpha(flashAlpha)
            )
        }
    }
}
