package com.example.stockmanager.utils

import androidx.compose.runtime.Composable

interface ImagePickerLauncher {
    fun pickImage()
    fun takePhoto()
}

@Composable
expect fun rememberImagePicker(onImagePicked: (ByteArray) -> Unit): ImagePickerLauncher
