// File: viewmodel/PendingUploadsViewModel.kt
package com.example.famlinks.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.famlinks.model.PendingUploadItem
import com.example.famlinks.model.UploadStatus
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import com.example.famlinks.data.upload.UploadManager
import com.example.famlinks.util.AppPreferences

class PendingUploadsViewModel : ViewModel() {

    private val _pendingUploads = MutableStateFlow<List<PendingUploadItem>>(emptyList())
    val pendingUploads: StateFlow<List<PendingUploadItem>> = _pendingUploads

    fun addItem(item: PendingUploadItem, context: Context) {
        _pendingUploads.update { it + item }
        saveToDisk(context)

        // Automatically start upload after item is added
        val allowCellular = AppPreferences.isDataAllowed(context)
        UploadManager.startUploading(context, this, allowCellular)
    }

    fun markAsUploading(id: String, context: Context? = null) {
        _pendingUploads.update { list ->
            list.map {
                if (it.id == id) it.copy(uploadStatus = UploadStatus.UPLOADING) else it
            }
        }
        context?.let { saveToDisk(it) }
    }

    fun markAsUploaded(id: String, context: Context? = null) {
        _pendingUploads.update { list ->
            list.map {
                if (it.id == id) it.copy(uploadStatus = UploadStatus.COMPLETE) else it
            }
        }
        context?.let { saveToDisk(it) }
    }

    fun markAsFailed(id: String, context: Context? = null) {
        _pendingUploads.update { list ->
            list.map {
                if (it.id == id) it.copy(uploadStatus = UploadStatus.FAILED) else it
            }
        }
        context?.let { saveToDisk(it) }
    }

    fun removeItem(id: String, context: Context) {
        _pendingUploads.update { list ->
            list.filterNot { it.id == id }
        }
        saveToDisk(context)
    }

    fun saveToDisk(context: Context) {
        val gson = Gson()
        val json = gson.toJson(_pendingUploads.value.map { it.copy(file = null) })
        val file = File(context.filesDir, "pending_uploads.json")
        file.writeText(json)
    }

    fun loadFromDisk(context: Context) {
        val file = File(context.filesDir, "pending_uploads.json")
        if (!file.exists()) return

        val json = file.readText()
        val gson = Gson()
        val type = object : TypeToken<List<PendingUploadItem>>() {}.type
        val loadedList: List<PendingUploadItem> = gson.fromJson(json, type)

        val restored = loadedList.mapNotNull {
            val f = File(it.localPath)
            if (f.exists()) it.copy(file = f) else null
        }
        _pendingUploads.value = restored
    }
}

