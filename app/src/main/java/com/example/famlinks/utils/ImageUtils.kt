// File: utils/ImageUtils.kt
package com.example.famlinks.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import java.io.File
import java.io.FileOutputStream

fun getImageResolution(file: File): String {
    return try {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, options)
        "${options.outWidth}x${options.outHeight}"
    } catch (e: Exception) {
        "Unknown"
    }
}

fun correctOrientationBitmap(file: File): Bitmap {
    val exif = ExifInterface(file.absolutePath)
    val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    val bitmap = BitmapFactory.decodeFile(file.absolutePath)

    val matrix = Matrix().apply {
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> postRotate(270f)
        }
    }

    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

/**
 * Compress a photo to 1080p width while preserving aspect ratio.
 */
fun compressTo1080p(inputFile: File): File {
    return try {
        val bitmap = correctOrientationBitmap(inputFile)
        val targetWidth = 1080
        val aspectRatio = bitmap.height.toDouble() / bitmap.width.toDouble()
        val targetHeight = (targetWidth * aspectRatio).toInt()

        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
        val compressedFile = File(inputFile.parent, inputFile.nameWithoutExtension + "_1080p.jpg")

        FileOutputStream(compressedFile).use { out ->
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }

        compressedFile
    } catch (e: Exception) {
        e.printStackTrace()
        inputFile
    }
}

/**
 * Generate a 500px-wide thumbnail while preserving aspect ratio.
 */
fun generateThumbnail(inputFile: File, maxWidth: Int = 500): File {
    return try {
        val bitmap = correctOrientationBitmap(inputFile)
        val aspectRatio = bitmap.height.toDouble() / bitmap.width.toDouble()
        val targetHeight = (maxWidth * aspectRatio).toInt()

        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, maxWidth, targetHeight, true)
        val thumbnailFile = File(inputFile.parent, inputFile.nameWithoutExtension + "_thumb.jpg")

        FileOutputStream(thumbnailFile).use { out ->
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
        }

        thumbnailFile
    } catch (e: Exception) {
        e.printStackTrace()
        inputFile
    }
}
