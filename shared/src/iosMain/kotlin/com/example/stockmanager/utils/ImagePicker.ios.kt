package com.example.stockmanager.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberImagePicker(onImagePicked: (ByteArray) -> Unit): ImagePickerLauncher {
    return remember {
        object : ImagePickerLauncher {
            override fun pickImage() {
                // En iOS real, se presentaría un UIImagePickerController o PHPickerViewController.
                // Para este ejemplo y compatibilidad multiplataforma simplificada, se define como stub.
            }

            override fun takePhoto() {
                // En iOS real, se presentaría la cámara usando UIImagePickerController con sourceType = Camera.
            }
        }
    }
}
