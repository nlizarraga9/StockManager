package com.example.stockmanager.utils

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image
import platform.Foundation.NSData
import platform.Foundation.create
import platform.Foundation.base64EncodedStringWithOptions
import platform.Foundation.NSDataBase64DecodingIgnoreUnknownCharacters
import platform.posix.memcpy
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned

@OptIn(ExperimentalForeignApi::class)
actual fun ByteArray.toImageBitmap(): ImageBitmap {
    return Image.makeFromEncoded(this).toComposeImageBitmap()
}

@OptIn(ExperimentalForeignApi::class)
actual object Base64Factory {
    actual fun decode(src: String): ByteArray {
        val nsData = NSData.create(base64EncodedString = src, options = NSDataBase64DecodingIgnoreUnknownCharacters)
            ?: throw IllegalArgumentException("Invalid Base64 string")
        val size = nsData.length.toInt()
        val byteArray = ByteArray(size)
        if (size > 0) {
            byteArray.usePinned { pinned ->
                memcpy(pinned.addressOf(0), nsData.bytes, nsData.length)
            }
        }
        return byteArray
    }

    actual fun encode(src: ByteArray): String {
        if (src.isEmpty()) return ""
        val nsData = src.usePinned { pinned ->
            NSData.create(bytes = pinned.addressOf(0), length = src.size.toULong())
        }
        return nsData.base64EncodedStringWithOptions(0UL)
    }
}
