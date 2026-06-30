package com.example.stockmanager

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.stockmanager.di.appModule
import com.example.stockmanager.presentation.productos.lista.ProductoListScreen
import org.koin.compose.KoinApplication

@Suppress("ktlint:standard:function-naming")
@Composable
fun App() {
    KoinApplication(application = {
        modules(appModule)
    }) {
        MaterialTheme {
            val navController = rememberNavController()
            NavHost(
                navController = navController,
                startDestination = "productos",
            ) {
                composable("productos") {
                    ProductoListScreen(navController = navController)
                }
                // Día 2: más destinos acá
            }
        }
    }
}
