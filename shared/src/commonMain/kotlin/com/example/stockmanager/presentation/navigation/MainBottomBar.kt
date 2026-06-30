package com.example.stockmanager.presentation.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import org.jetbrains.compose.resources.painterResource
import stockmanager.shared.generated.resources.Res
import stockmanager.shared.generated.resources.inventory
import stockmanager.shared.generated.resources.shopping_cart

sealed class BottomDestination(
    val route: String,
    val label: String,
) {
    data object Productos : BottomDestination("productos", "Productos")

    data object Ventas : BottomDestination("ventas", "Ventas")
}

private val bottomDestinations = listOf(BottomDestination.Productos, BottomDestination.Ventas)

/**
 * Rutas en las que se debe mostrar la bottom bar (pantallas de nivel superior).
 * El resto de las rutas (detalle, formularios, nueva venta) la ocultan.
 */
val rutasConBottomBar = bottomDestinations.map { it.route }.toSet()

@Suppress("ktlint:standard:function-naming")
@Composable
fun MainBottomBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar {
        bottomDestinations.forEach { destination ->
            val selected =
                currentDestination?.hierarchy?.any { it.route == destination.route } == true

            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        navController.navigate(destination.route) {
                            navController.graph.findStartDestination().route?.let { startRoute ->
                                popUpTo(startRoute) {
                                    saveState = true
                                }
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        painter =
                            painterResource(
                                when (destination) {
                                    is BottomDestination.Productos -> Res.drawable.inventory
                                    is BottomDestination.Ventas -> Res.drawable.shopping_cart
                                },
                            ),
                        contentDescription = destination.label,
                    )
                },
                label = { Text(destination.label) },
            )
        }
    }
}
