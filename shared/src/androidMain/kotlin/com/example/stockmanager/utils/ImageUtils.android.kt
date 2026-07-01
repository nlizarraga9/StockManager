package com.example.stockmanager.utils

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import android.util.Base64

actual fun ByteArray.toImageBitmap(): ImageBitmap {
    val bitmap = BitmapFactory.decodeByteArray(this, 0, this.size)
        ?: throw IllegalArgumentException("Could not decode image bytes")
    return bitmap.asImageBitmap()
}

actual object Base64Factory {
    actual fun decode(src: String): ByteArray {
        return Base64.decode(src, Base64.DEFAULT)
    }

    actual fun encode(src: ByteArray): String {
        return Base64.encodeToString(src, Base64.NO_WRAP)
    }
}
