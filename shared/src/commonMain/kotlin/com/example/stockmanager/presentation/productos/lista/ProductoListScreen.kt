package com.example.stockmanager.presentation.productos.lista

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.stockmanager.data.remote.ProductoDto
import com.example.stockmanager.data.remote.toDomain
import com.example.stockmanager.domain.model.Producto
import com.example.stockmanager.presentation.components.ProductoCard
import kotlinx.serialization.json.Json.Default.decodeFromString
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import stockmanager.shared.generated.resources.Res
import stockmanager.shared.generated.resources.add
import stockmanager.shared.generated.resources.shopping_cart

@Suppress("ktlint:standard:function-naming")
@Composable
fun ProductoListScreen(
    navController: NavController,
    snackbarHostState: SnackbarHostState
) {
    val viewModel = koinViewModel<ProductoListViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val sortMode by viewModel.sortMode.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.cargarProductos(forceSilent = true)
    }

    LaunchedEffect(savedStateHandle) {
        savedStateHandle
            ?.getStateFlow<String?>("producto_action_success", null)
            ?.collect { action ->
                if (action != null) {
                    savedStateHandle.remove<String>("producto_action_success")
                    val mensaje =
                        if (action == "edit") {
                            "Producto editado con éxito"
                        } else {
                            "Producto agregado con éxito"
                        }
                    snackbarHostState.showSnackbar(mensaje)
                }
            }
    }

    LaunchedEffect(savedStateHandle) {
        savedStateHandle
            ?.getStateFlow<String?>("deleted_product", null)
            ?.collect { deletedJson ->
                if (deletedJson != null) {
                    savedStateHandle.remove<String>("deleted_product")
                    try {
                        val productDto =
                            decodeFromString(
                                ProductoDto.serializer(),
                                deletedJson,
                            )
                        val producto = productDto.toDomain()
                        val result =
                            snackbarHostState.showSnackbar(
                                message = "Producto \"${producto.nombre}\" eliminado",
                                actionLabel = "Deshacer",
                                duration = SnackbarDuration.Long,
                            )
                        if (result == SnackbarResult.ActionPerformed) {
                            viewModel.deshacerEliminar(producto)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
    }

    ProductoListContent(
        state = state,
        sortMode = sortMode,
        searchQuery = searchQuery,
        onSortModeChange = { viewModel.setSortMode(it) },
        onSearchQueryChange = { viewModel.setSearchQuery(it) },
        onRetry = { viewModel.cargarProductos(forceSilent = false) },
        onAgregarClick = { navController.navigate("producto/nuevo") },
        onProductoClick = { id -> navController.navigate("producto/$id/detalle") },
    )
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun ProductoListContent(
    state: ProductoListState,
    sortMode: ProductSortMode,
    searchQuery: String,
    onSortModeChange: (ProductSortMode) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onRetry: () -> Unit,
    onAgregarClick: () -> Unit,
    onProductoClick: (String) -> Unit,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(sortMode, searchQuery) {
        listState.scrollToItem(0)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Stock Almacén") },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAgregarClick,
                icon = {
                    Icon(
                        painter = painterResource(Res.drawable.add),
                        contentDescription = null,
                        modifier = Modifier.size(26.dp),
                    )
                },
                text = { Text("Nuevo producto") },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.offset(y = 10.dp),
            )
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(top = padding.calculateTopPadding()),
        ) {
            // Buscador
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Buscar producto…") },
                singleLine = true,
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Text("✕", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
            )

            // Chips de ordenación
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ProductSortMode.entries.forEach { mode ->
                    val selected = sortMode == mode
                    val backgroundColor =
                        if (selected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                                .copy(
                                    alpha = 0.5f,
                                )
                        }
                    val contentColor =
                        if (selected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }

                    Surface(
                        onClick = { onSortModeChange(mode) },
                        selected = selected,
                        shape = RoundedCornerShape(16.dp),
                        color = backgroundColor,
                        contentColor = contentColor,
                        modifier = Modifier.weight(1f),
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = mode.displayName,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
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
                        if (s.productos.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "No se encontraron productos",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        } else {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding =
                                    PaddingValues(
                                        top = 8.dp,
                                        bottom = 80.dp,
                                    ),
                            ) {
                                items(s.productos, key = { it.id }) { producto ->
                                    ProductoCard(
                                        producto = producto,
                                        onClick = { onProductoClick(producto.id) },
                                    )
                                }
                            }
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
            sortMode = ProductSortMode.ALPHABETICAL,
            searchQuery = "",
            onSortModeChange = {},
            onSearchQueryChange = {},
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
            sortMode = ProductSortMode.ALPHABETICAL,
            searchQuery = "",
            onSortModeChange = {},
            onSearchQueryChange = {},
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
            sortMode = ProductSortMode.ALPHABETICAL,
            searchQuery = "",
            onSortModeChange = {},
            onSearchQueryChange = {},
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
            sortMode = ProductSortMode.ALPHABETICAL,
            searchQuery = "",
            onSortModeChange = {},
            onSearchQueryChange = {},
            onRetry = {},
            onAgregarClick = {},
            onProductoClick = {},
        )
    }
}
