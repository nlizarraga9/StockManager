package com.example.stockmanager

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.stockmanager.di.appModule
import com.example.stockmanager.presentation.navigation.MainBottomBar
import com.example.stockmanager.presentation.navigation.rutasConBottomBar
import com.example.stockmanager.presentation.productos.detalle.ProductoDetailScreen
import com.example.stockmanager.presentation.productos.form.ProductoFormScreen
import com.example.stockmanager.presentation.productos.lista.ProductoListScreen
import com.example.stockmanager.presentation.venta.detalle.VentaDetailScreen
import com.example.stockmanager.presentation.venta.lista.VentaListScreen
import com.example.stockmanager.presentation.venta.nueva.VentaScreen
import org.koin.compose.KoinApplication

@Suppress("ktlint:standard:function-naming")
@Composable
fun App() {
    KoinApplication(application = {
        modules(appModule)
    }) {
        MaterialTheme {
            val navController = rememberNavController()
            val backStackEntry by navController.currentBackStackEntryAsState()
            val rutaActual = backStackEntry?.destination?.route
            val mostrarBottomBar = rutaActual in rutasConBottomBar

            Scaffold(
                bottomBar = {
                    if (mostrarBottomBar) {
                        MainBottomBar(navController = navController)
                    }
                },
            ) { padding ->
                NavHost(
                    navController = navController,
                    startDestination = "productos",
                    modifier = Modifier.padding(padding),
                ) {
                    // Listado de productos
                    composable("productos") {
                        ProductoListScreen(navController = navController)
                    }

                    // Detalle de producto
                    composable(
                        route = "producto/{id}/detalle",
                        arguments = listOf(navArgument("id") { type = NavType.StringType }),
                    ) { backStack ->
                        val id = backStack.arguments?.getString("id").orEmpty()
                        ProductoDetailScreen(navController = navController, productoId = id)
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

                    // Listado de ventas
                    composable("ventas") {
                        VentaListScreen(navController = navController)
                    }

                    // Detalle de venta
                    composable(
                        route = "venta/{id}/detalle",
                        arguments = listOf(navArgument("id") { type = NavType.StringType }),
                    ) { backStack ->
                        val id = backStack.arguments?.getString("id").orEmpty()
                        VentaDetailScreen(navController = navController, ventaId = id)
                    }

                    // Nueva venta
                    composable("venta/nueva") {
                        VentaScreen(navController = navController)
                    }
                }
            }
        }
    }
}
