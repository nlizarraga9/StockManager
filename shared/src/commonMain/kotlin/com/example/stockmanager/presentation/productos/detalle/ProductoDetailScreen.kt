package com.example.stockmanager.presentation.productos.detalle

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.stockmanager.domain.model.Producto
import com.example.stockmanager.utils.decodeBase64ToBitmap
import com.example.stockmanager.utils.toPrice
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import stockmanager.shared.generated.resources.Res
import stockmanager.shared.generated.resources.arrow_back
import stockmanager.shared.generated.resources.delete
import stockmanager.shared.generated.resources.inventory
import stockmanager.shared.generated.resources.sin_imagen

@Suppress("ktlint:standard:function-naming")
@Composable
fun ProductoDetailScreen(
    navController: NavController,
    productoId: String,
) {
    val viewModel = koinViewModel<ProductoDetailViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(productoId) {
        viewModel.cargarProducto(productoId)
    }

    LaunchedEffect(state) {
        when (val s = state) {
            is ProductoDetailState.Deleted -> {
                navController.popBackStack()
            }

            is ProductoDetailState.Error -> {
                snackbarHostState.showSnackbar(s.mensaje)
                viewModel.resetError()
                viewModel.cargarProducto(productoId)
            }

            else -> {
                Unit
            }
        }
    }

    ProductoDetailContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onEditar = { navController.navigate("producto/$productoId/editar") },
        onEliminar = { viewModel.eliminar(productoId) },
        onBack = { navController.popBackStack() },
    )
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun ProductoDetailContent(
    state: ProductoDetailState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onEditar: () -> Unit = {},
    onEliminar: () -> Unit = {},
    onBack: () -> Unit = {},
) {
    var mostrarConfirmacionEliminar by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del producto") },
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        when (state) {
            is ProductoDetailState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator() }
            }

            is ProductoDetailState.Deleting -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(12.dp))
                        Text("Eliminando producto…", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            is ProductoDetailState.Loaded -> {
                val producto = state.producto
                val stockColor =
                    when {
                        producto.sinStock -> MaterialTheme.colorScheme.error
                        producto.stockBajo -> Color(0xFFFF9800)
                        else -> MaterialTheme.colorScheme.primary
                    }

                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(16.dp),
                ) {
                    // Imagen del producto
                    val imageBitmap = producto.imagenUrl?.decodeBase64ToBitmap()
                    Card(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (imageBitmap != null) {
                                Image(
                                    bitmap = imageBitmap,
                                    contentDescription = producto.nombre,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                )
                            } else {
                                Image(
                                    painter = painterResource(Res.drawable.sin_imagen),
                                    contentDescription = "Sin imagen",
                                    modifier = Modifier.size(90.dp),
                                    contentScale = ContentScale.Fit,
                                    alpha = 0.5f,
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = producto.nombre,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    producto.descripcion?.let {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            DetailRow(label = "Precio", value = producto.precio.toPrice())
                            Spacer(Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text("Stock actual", style = MaterialTheme.typography.bodyMedium)
                                Surface(
                                    color = stockColor.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp),
                                ) {
                                    Text(
                                        text = "${producto.stock} unidades",
                                        color = stockColor,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelMedium,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    )
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            DetailRow(label = "Stock mínimo", value = "${producto.stockMinimo} unidades")

                            if (producto.sinStock || producto.stockBajo) {
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    text = if (producto.sinStock) "⚠ Sin stock disponible" else "⚠ Stock por debajo del mínimo",
                                    color = stockColor,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    }

                    Spacer(Modifier.weight(1f))

                    Button(
                        onClick = onEditar,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Editar producto")
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { mostrarConfirmacionEliminar = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors =
                            ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error,
                            ),
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.delete),
                            contentDescription = null,
                            modifier = Modifier.size(26.dp).padding(end = 4.dp),
                        )
                        Text("Eliminar producto")
                    }
                }

                if (mostrarConfirmacionEliminar) {
                    AlertDialog(
                        onDismissRequest = { mostrarConfirmacionEliminar = false },
                        title = { Text("Eliminar producto") },
                        text = {
                            Text("¿Seguro que querés eliminar \"${producto.nombre}\"? Esta acción no se puede deshacer.")
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    mostrarConfirmacionEliminar = false
                                    onEliminar()
                                },
                                colors =
                                    ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error,
                                    ),
                            ) { Text("Eliminar") }
                        },
                        dismissButton = {
                            TextButton(onClick = { mostrarConfirmacionEliminar = false }) {
                                Text("Cancelar")
                            }
                        },
                    )
                }
            }

            is ProductoDetailState.Error, is ProductoDetailState.Deleted -> {
                Unit
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun DetailRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

// ---- PREVIEWS ----

private val productoEjemplo =
    Producto(
        id = "1",
        nombre = "Coca Cola 2.25L",
        descripcion = "Gaseosa cola",
        precio = 1500.0,
        stock = 24,
        stockMinimo = 5,
    )

private val productoStockBajoEjemplo = productoEjemplo.copy(stock = 3)
private val productoSinStockEjemplo = productoEjemplo.copy(stock = 0)

@Preview(showBackground = true, showSystemUi = true)
@Suppress("ktlint:standard:function-naming")
@Composable
fun ProductoDetailNormalPreview() {
    MaterialTheme {
        ProductoDetailContent(state = ProductoDetailState.Loaded(productoEjemplo))
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Suppress("ktlint:standard:function-naming")
@Composable
fun ProductoDetailStockBajoPreview() {
    MaterialTheme {
        ProductoDetailContent(state = ProductoDetailState.Loaded(productoStockBajoEjemplo))
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Suppress("ktlint:standard:function-naming")
@Composable
fun ProductoDetailSinStockPreview() {
    MaterialTheme {
        ProductoDetailContent(state = ProductoDetailState.Loaded(productoSinStockEjemplo))
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Suppress("ktlint:standard:function-naming")
@Composable
fun ProductoDetailLoadingPreview() {
    MaterialTheme {
        ProductoDetailContent(state = ProductoDetailState.Loading)
    }
}
