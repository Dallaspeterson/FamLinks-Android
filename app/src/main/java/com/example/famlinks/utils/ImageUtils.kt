package com.example.famlinks.utils

import android.graphics.BitmapFactory
import java.io.File

fun getImageResolution(file: File): String {
    return try {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, options)
        "${options.outWidth}x${options.outHeight}"
    } catch (e: Exception) {
        "Unknown"
    }
}
