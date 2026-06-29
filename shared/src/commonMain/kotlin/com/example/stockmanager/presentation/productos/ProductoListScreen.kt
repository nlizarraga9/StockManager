package com.example.stockmanager.presentation.productos

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.stockmanager.domain.model.Producto
import com.example.stockmanager.presentation.components.ProductoCard
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import stockmanager.shared.generated.resources.Res
import stockmanager.shared.generated.resources.add

@Suppress("ktlint:standard:function-naming")
@Composable
fun ProductoListScreen(navController: NavController) {
    val viewModel = koinViewModel<ProductoListViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    ProductoListContent(
        state = state,
        onRetry = { viewModel.cargarProductos() },
        onAgregarClick = {},
        onProductoClick = {},
    )
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun ProductoListContent(
    state: ProductoListState,
    onRetry: () -> Unit,
    onAgregarClick: () -> Unit,
    onProductoClick: (String) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Stock Almacén") },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* TODO: navegar a formulario */ }) {
                Icon(
                    painter = painterResource(Res.drawable.add),
                    contentDescription = "Agregar producto",
                )
            }
        },
    ) { padding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding),
        ) {
            when (val s = state) {
                is ProductoListState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is ProductoListState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "Ocurrió un error",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = s.mensaje,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = onRetry) {
                            Text("Reintentar")
                        }
                    }
                }

                is ProductoListState.Empty -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "No hay productos",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Tocá el botón + para agregar el primero",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                is ProductoListState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp),
                    ) {
                        items(s.productos, key = { it.id }) { producto ->
                            ProductoCard(
                                producto = producto,
                                onClick = { /* TODO: navegar a detalle */ },
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---- PREVIEWS ---------
private val productosEjemplo =
    listOf(
        Producto("1", "Coca Cola 2.25L", "Gaseosa", 1500.0, 24, 5),
        Producto("2", "Arroz Gallo 1kg", "Arroz largo fino", 900.0, 3, 5),
        Producto("3", "Fideos Matarazzo 500g", "Fideos tallarines", 750.0, 0, 5),
    )

@Suppress("ktlint:standard:function-naming")
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProductoListSuccessPreview() {
    MaterialTheme {
        ProductoListContent(
            state = ProductoListState.Success(productosEjemplo),
            onRetry = {},
            onAgregarClick = {},
            onProductoClick = {},
        )
    }
}

@Suppress("ktlint:standard:function-naming")
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProductoListLoadingPreview() {
    MaterialTheme {
        ProductoListContent(
            state = ProductoListState.Loading,
            onRetry = {},
            onAgregarClick = {},
            onProductoClick = {},
        )
    }
}

@Suppress("ktlint:standard:function-naming")
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProductoListEmptyPreview() {
    MaterialTheme {
        ProductoListContent(
            state = ProductoListState.Empty,
            onRetry = {},
            onAgregarClick = {},
            onProductoClick = {},
        )
    }
}

@Suppress("ktlint:standard:function-naming")
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProductoListErrorPreview() {
    MaterialTheme {
        ProductoListContent(
            state = ProductoListState.Error("No se pudo conectar con el servidor"),
            onRetry = {},
            onAgregarClick = {},
            onProductoClick = {},
        )
    }
}
