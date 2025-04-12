// File: viewmodel/CameraViewModel.kt
package com.example.famlinks.viewmodel

import androidx.camera.core.CameraSelector
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import android.net.Uri

class CameraViewModel : ViewModel() {

    private val _lensFacing = MutableStateFlow(CameraSelector.LENS_FACING_BACK)
    val lensFacing: StateFlow<Int> = _lensFacing

    private val _flashEnabled = MutableStateFlow(false)
    val flashEnabled: StateFlow<Boolean> = _flashEnabled

    private val _lastPhotoUri = MutableStateFlow<Uri?>(null)
    val lastPhotoUri: StateFlow<Uri?> = _lastPhotoUri

    fun toggleCamera() {
        _lensFacing.value = if (_lensFacing.value == CameraSelector.LENS_FACING_BACK)
            CameraSelector.LENS_FACING_FRONT
        else
            CameraSelector.LENS_FACING_BACK
    }

    fun toggleFlash() {
        _flashEnabled.value = !_flashEnabled.value
    }

    fun setFlash(enabled: Boolean) {
        _flashEnabled.value = enabled
    }

    fun setLastPhotoUri(uri: Uri?) {
        _lastPhotoUri.value = uri
    }
}