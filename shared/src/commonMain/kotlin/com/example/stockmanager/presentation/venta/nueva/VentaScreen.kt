package com.example.stockmanager.presentation.venta.nueva

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.stockmanager.domain.model.Producto
import com.example.stockmanager.domain.model.Venta
import com.example.stockmanager.domain.model.VentaItem
import com.example.stockmanager.utils.toPrice
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import stockmanager.shared.generated.resources.Res
import stockmanager.shared.generated.resources.add
import stockmanager.shared.generated.resources.arrow_back
import stockmanager.shared.generated.resources.delete
import stockmanager.shared.generated.resources.remove

@Suppress("ktlint:standard:function-naming")
@Composable
fun VentaScreen(
    navController: NavController,
    snackbarHostState: SnackbarHostState,
) {
    val viewModel = koinViewModel<VentaViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state) {
        when (val s = state) {
            is VentaState.VentaExitosa -> {
                navController.previousBackStackEntry?.savedStateHandle?.set("venta_action_success", "add")
                navController.popBackStack()
            }

            is VentaState.Error -> {
                snackbarHostState.showSnackbar(s.mensaje)
                viewModel.resetError()
            }

            else -> {
                Unit
            }
        }
    }

    VentaContent(
        state = state,
        onAgregarAlCarrito = { producto, cantidad -> viewModel.agregarAlCarrito(producto, cantidad) },
        onIncrementar = { id, cantidad -> viewModel.cambiarCantidad(id, cantidad + 1) },
        onDecrementar = { id, cantidad -> viewModel.cambiarCantidad(id, cantidad - 1) },
        onEliminar = { id -> viewModel.quitarDelCarrito(id) },
        onConfirmarVenta = { viewModel.confirmarVenta() },
        onBack = { navController.popBackStack() },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("ktlint:standard:function-naming")
@Composable
fun VentaContent(
    state: VentaState,
    onAgregarAlCarrito: (Producto, Int) -> Unit = { _, _ -> },
    onIncrementar: (String, Int) -> Unit = { _, _ -> },
    onDecrementar: (String, Int) -> Unit = { _, _ -> },
    onEliminar: (String) -> Unit = {},
    onConfirmarVenta: () -> Unit = {},
    onBack: () -> Unit = {},
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var mostrarSelectorProducto by remember { mutableStateOf(false) }
    var mostrarConfirmacion by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Nueva venta") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(Res.drawable.arrow_back),
                            contentDescription = "Volver",
                            modifier = Modifier.size(28.dp),
                        )
                    }
                },
            )
        },
    ) { padding ->
        when (val s = state) {
            is VentaState.LoadingProductos -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator() }
            }

            is VentaState.Guardando -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(12.dp))
                        Text("Confirmando venta…", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            is VentaState.Idle -> {
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(padding),
                ) {
                    // Lista de items del carrito
                    if (s.itemsCarrito.isEmpty()) {
                        Box(
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "El carrito está vacío",
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Tocá \"Agregar producto\" para empezar",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 8.dp),
                        ) {
                            items(s.itemsCarrito, key = { it.productoId }) { item ->
                                CarritoItemRow(
                                    item = item,
                                    onIncrementar = { onIncrementar(item.productoId, item.cantidad) },
                                    onDecrementar = { onDecrementar(item.productoId, item.cantidad) },
                                    onEliminar = { onEliminar(item.productoId) },
                                )
                            }
                        }
                    }

                    // Panel inferior
                    Surface(
                        shadowElevation = 8.dp,
                        color = MaterialTheme.colorScheme.surfaceContainer,
                    ) {
                        Column(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                        ) {
                            if (s.itemsCarrito.isNotEmpty()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(
                                        "Total",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Text(
                                        Venta.calcularTotal(s.itemsCarrito).toPrice(),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                                Spacer(Modifier.height(12.dp))
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                TextButton(
                                    onClick = { mostrarSelectorProducto = true },
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Icon(
                                        painter = painterResource(Res.drawable.add),
                                        contentDescription = null,
                                        modifier = Modifier.size(26.dp),
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text("Agregar")
                                }
                                if (s.itemsCarrito.isNotEmpty()) {
                                    Button(
                                        onClick = { mostrarConfirmacion = true },
                                        modifier = Modifier.weight(1f),
                                    ) {
                                        Text("Confirmar venta")
                                    }
                                }
                            }
                        }
                    }
                }

                // Bottom sheet selector de producto
                if (mostrarSelectorProducto) {
                    ModalBottomSheet(
                        onDismissRequest = { mostrarSelectorProducto = false },
                        sheetState = sheetState,
                    ) {
                        SelectorProductoSheet(
                            productos = s.productos,
                            onSeleccionar = { producto, cantidad ->
                                onAgregarAlCarrito(producto, cantidad)
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    mostrarSelectorProducto = false
                                }
                            },
                        )
                    }
                }

                // Diálogo de confirmación
                if (mostrarConfirmacion) {
                    val total = Venta.calcularTotal(s.itemsCarrito).toPrice()
                    val cantProductos = s.itemsCarrito.size
                    AlertDialog(
                        onDismissRequest = { mostrarConfirmacion = false },
                        title = { Text("Confirmar venta") },
                        text = {
                            Text(
                                "¿Confirmás la venta por $total?\n\n" +
                                    "Se descontará el stock de $cantProductos " +
                                    if (cantProductos == 1) "producto." else "productos.",
                            )
                        },
                        confirmButton = {
                            Button(onClick = {
                                mostrarConfirmacion = false
                                onConfirmarVenta()
                            }) { Text("Confirmar") }
                        },
                        dismissButton = {
                            TextButton(onClick = { mostrarConfirmacion = false }) {
                                Text("Cancelar")
                            }
                        },
                    )
                }
            }

            else -> {
                Unit
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun CarritoItemRow(
    item: VentaItem,
    onIncrementar: () -> Unit,
    onDecrementar: () -> Unit,
    onEliminar: () -> Unit,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.nombreProducto,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "${item.precioUnitario.toPrice()} c/u",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "Subtotal: ${item.subtotal.toPrice()}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onEliminar) {
                    Icon(
                        painter = painterResource(Res.drawable.delete),
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(26.dp),
                    )
                }
                IconButton(onClick = onDecrementar) {
                    Icon(
                        painter = painterResource(Res.drawable.remove),
                        contentDescription = "Decrementar",
                        modifier = Modifier.size(26.dp),
                    )
                }
                Text(
                    text = item.cantidad.toString(),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(28.dp),
                    textAlign = TextAlign.Center,
                )
                IconButton(onClick = onIncrementar) {
                    Icon(
                        painter = painterResource(Res.drawable.add),
                        contentDescription = "Incrementar",
                        modifier = Modifier.size(26.dp),
                    )
                }
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
internal fun SelectorProductoSheet(
    productos: List<Producto>,
    onSeleccionar: (Producto, Int) -> Unit,
) {
    var productoSeleccionado by remember { mutableStateOf<Producto?>(null) }
    var cantidad by remember { mutableStateOf(1) }

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
    ) {
        Text(
            text = "Seleccionar producto",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        )
        HorizontalDivider()

        if (productoSeleccionado == null) {
            val disponibles = productos
                .filter { it.stock > 0 }
                .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.nombre })
            LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
                if (disponibles.isEmpty()) {
                    item {
                        Text(
                            "No hay productos con stock disponible",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                items(disponibles, key = { it.id }) { producto ->
                    TextButton(
                        onClick = {
                            productoSeleccionado = producto
                            cantidad = 1
                        },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    producto.nombre,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    producto.precio.toPrice(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Text(
                                "Stock: ${producto.stock}",
                                style = MaterialTheme.typography.labelSmall,
                                color =
                                    if (producto.stockBajo) {
                                        Color(0xFFFF9800)
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    },
                            )
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        } else {
            val producto = productoSeleccionado!!
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(producto.nombre, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(
                    producto.precio.toPrice(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    "Disponible: ${producto.stock}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(24.dp))
                Text("Cantidad", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    IconButton(onClick = { if (cantidad > 1) cantidad-- }, enabled = cantidad > 1) {
                        Icon(
                            painter = painterResource(Res.drawable.remove),
                            contentDescription = "Menos",
                            modifier = Modifier.size(26.dp),
                        )
                    }
                    Text(cantidad.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { if (cantidad < producto.stock) cantidad++ }, enabled = cantidad < producto.stock) {
                        Icon(
                            painter = painterResource(Res.drawable.add),
                            contentDescription = "Más",
                            modifier = Modifier.size(26.dp),
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "Subtotal: ${(producto.precio * cantidad).toPrice()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { productoSeleccionado = null }) { Text("← Volver") }
                    Button(onClick = { onSeleccionar(producto, cantidad) }) { Text("Agregar al carrito") }
                }
            }
        }
    }
}

// ---- Datos de ejemplo para previews ----

private val productosEjemplo =
    listOf(
        Producto("1", "Coca Cola 2.25L", "Gaseosa", 1500.0, 24, 5),
        Producto("2", "Arroz Gallo 1kg", "Arroz largo fino", 900.0, 3, 5),
        Producto("3", "Fideos Matarazzo 500g", null, 750.0, 0, 5),
    )

private val carritoEjemplo =
    listOf(
        VentaItem(productoId = "1", nombreProducto = "Coca Cola 2.25L", cantidad = 2, precioUnitario = 1500.0),
        VentaItem(productoId = "2", nombreProducto = "Arroz Gallo 1kg", cantidad = 1, precioUnitario = 900.0),
    )

// ---- PREVIEWS ----

@Preview(showBackground = true, showSystemUi = true)
@Suppress("ktlint:standard:function-naming")
@Composable
fun VentaLoadingPreview() {
    MaterialTheme {
        VentaContent(state = VentaState.LoadingProductos)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Suppress("ktlint:standard:function-naming")
@Composable
fun VentaCarritoVacioPreview() {
    MaterialTheme {
        VentaContent(state = VentaState.Idle(productos = productosEjemplo, itemsCarrito = emptyList()))
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Suppress("ktlint:standard:function-naming")
@Composable
fun VentaConItemsPreview() {
    MaterialTheme {
        VentaContent(
            state =
                VentaState.Idle(
                    productos = productosEjemplo,
                    itemsCarrito = carritoEjemplo,
                ),
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Suppress("ktlint:standard:function-naming")
@Composable
fun VentaGuardandoPreview() {
    MaterialTheme {
        VentaContent(state = VentaState.Guardando)
    }
}

@Preview(showBackground = true)
@Suppress("ktlint:standard:function-naming")
@Composable
fun SelectorProductoSheetPreview() {
    MaterialTheme {
        SelectorProductoSheet(
            productos = productosEjemplo,
            onSeleccionar = { _, _ -> },
        )
    }
}
