package com.example.stockmanager

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.stockmanager.di.appModule
import com.example.stockmanager.presentation.productos.form.ProductoFormScreen
import com.example.stockmanager.presentation.productos.lista.ProductoListScreen
import com.example.stockmanager.presentation.venta.VentaScreen
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
                // Listado de productos
                composable("productos") {
                    ProductoListScreen(navController = navController)
                }

                // Nuevo producto
                composable("producto/nuevo") {
                    ProductoFormScreen(
                        navController = navController,
                        productoId = null,
                    )
                }

                // Editar producto
                composable(
                    route = "producto/{id}/editar",
                    arguments = listOf(navArgument("id") { type = NavType.StringType }),
                ) { backStackEntry ->
                    val id = backStackEntry.arguments?.getString("id")
                    ProductoFormScreen(
                        navController = navController,
                        productoId = id,
                    )
                }

                // Nueva venta
                composable("venta/nueva") {
                    VentaScreen(navController = navController)
                }
            }
        }
    }
}
