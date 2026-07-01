package com.example.stockmanager.utils

import androidx.compose.ui.graphics.ImageBitmap

expect fun ByteArray.toImageBitmap(): ImageBitmap

expect object Base64Factory {
    fun decode(src: String): ByteArray
    fun encode(src: ByteArray): String
}

fun String.decodeBase64ToBitmap(): ImageBitmap? {
    return try {
        val cleanBase64 = if (this.startsWith("data:image")) {
            this.substringAfter("base64,")
        } else {
            this
        }
        val bytes = Base64Factory.decode(cleanBase64)
        bytes.toImageBitmap()
    } catch (e: Exception) {
        null
    }
}
