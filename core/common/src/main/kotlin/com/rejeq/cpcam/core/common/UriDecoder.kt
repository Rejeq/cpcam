package com.rejeq.cpcam.core.common

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import java.io.IOException

fun decodeBitmapFromUri(
    contentResolver: ContentResolver,
    fileUri: Uri,
): Bitmap? = try {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        ImageDecoder.decodeBitmap(
            ImageDecoder.createSource(contentResolver, fileUri),
        )
    } else {
        @Suppress("DEPRECATION")
        MediaStore.Images.Media.getBitmap(contentResolver, fileUri)
    }
} catch (e: IOException) {
    Log.e(TAG, "I/O error decoding bitmap from URI: $fileUri", e)
    null
} catch (e: SecurityException) {
    Log.e(TAG, "Security error accessing URI: $fileUri", e)
    null
} catch (e: IllegalArgumentException) {
    Log.e(TAG, "Invalid URI or unsupported image type: $fileUri", e)
    null
} catch (e: OutOfMemoryError) {
    Log.e(TAG, "OutOfMemory decoding bitmap from URI: $fileUri", e)
    null
}

private const val TAG = "UriDecoder"
