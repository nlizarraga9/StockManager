package com.example.stockmanager.presentation.venta.detalle

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.stockmanager.data.remote.VentaItemDto
import com.example.stockmanager.data.remote.toDto
import com.example.stockmanager.domain.model.Venta
import com.example.stockmanager.domain.model.VentaItem
import com.example.stockmanager.utils.toFechaLegible
import com.example.stockmanager.utils.toPrice
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json.Default.encodeToString
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import stockmanager.shared.generated.resources.Res
import stockmanager.shared.generated.resources.arrow_back
import stockmanager.shared.generated.resources.delete

@Suppress("ktlint:standard:function-naming")
@Composable
fun VentaDetailScreen(
    navController: NavController,
    ventaId: String,
    snackbarHostState: SnackbarHostState,
) {
    val viewModel = koinViewModel<VentaDetailViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    var lastVentaBeforeDelete by remember { mutableStateOf<Venta?>(null) }

    LaunchedEffect(ventaId) {
        viewModel.cargarVenta(ventaId)
    }

    LaunchedEffect(state) {
        when (val s = state) {
            is VentaDetailState.Loaded -> {
                lastVentaBeforeDelete = s.venta
            }

            is VentaDetailState.Deleted -> {
                lastVentaBeforeDelete?.let { v ->
                    try {
                        val listSerializer =
                            ListSerializer(
                                VentaItemDto.serializer(),
                            )
                        val json =
                            encodeToString(
                                listSerializer,
                                v.items.map { it.toDto(ventaIdOverride = "") },
                            )
                        navController.previousBackStackEntry?.savedStateHandle?.set("deleted_venta_items", json)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                navController.popBackStack()
            }

            is VentaDetailState.Error -> {
                snackbarHostState.showSnackbar(s.mensaje)
                viewModel.resetError()
                viewModel.cargarVenta(ventaId)
            }

            else -> {
                Unit
            }
        }
    }

    VentaDetailContent(
        state = state,
        onEliminar = { viewModel.eliminar(ventaId) },
        onBack = { navController.popBackStack() },
    )
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun VentaDetailContent(
    state: VentaDetailState,
    onEliminar: () -> Unit = {},
    onBack: () -> Unit = {},
) {
    var mostrarConfirmacionEliminar by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Detalle de venta") },
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
        when (state) {
            is VentaDetailState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator() }
            }

            is VentaDetailState.Deleting -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(12.dp))
                        Text("Eliminando venta y devolviendo stock…", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            is VentaDetailState.Loaded -> {
                val venta = state.venta
                Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = venta.createdAt.toFechaLegible(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "${venta.items.size} " + if (venta.items.size == 1) "producto" else "productos",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(venta.items, key = { it.id.ifBlank { it.productoId } }) { item ->
                            VentaItemRow(item)
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }

                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text("Total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                venta.total.toPrice(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = { mostrarConfirmacionEliminar = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.delete),
                                contentDescription = null,
                                modifier = Modifier.size(26.dp).padding(end = 4.dp),
                            )
                            Text("Eliminar venta")
                        }
                    }
                }

                if (mostrarConfirmacionEliminar) {
                    AlertDialog(
                        onDismissRequest = { mostrarConfirmacionEliminar = false },
                        title = { Text("Eliminar venta") },
                        text = {
                            Text(
                                "¿Seguro que querés eliminar esta venta? El stock de los " +
                                    "${venta.items.size} " +
                                    (if (venta.items.size == 1) "producto involucrado" else "productos involucrados") +
                                    " se va a devolver automáticamente.",
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    mostrarConfirmacionEliminar = false
                                    onEliminar()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
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

            is VentaDetailState.Error, is VentaDetailState.Deleted -> {
                Unit
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun VentaItemRow(item: VentaItem) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(item.nombreProducto, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(
                "${item.cantidad} x ${item.precioUnitario.toPrice()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            item.subtotal.toPrice(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

// ---- PREVIEWS ----

private val ventaEjemplo =
    Venta(
        id = "1",
        total = 3900.0,
        createdAt = "2026-06-30T13:45:12.000000+00:00",
        items =
            listOf(
                VentaItem(
                    productoId = "1",
                    nombreProducto = "Coca Cola 2.25L",
                    cantidad = 2,
                    precioUnitario = 1500.0,
                ),
                VentaItem(
                    productoId = "2",
                    nombreProducto = "Arroz Gallo 1kg",
                    cantidad = 1,
                    precioUnitario = 900.0,
                ),
            ),
    )

@Preview(showBackground = true, showSystemUi = true)
@Suppress("ktlint:standard:function-naming")
@Composable
fun VentaDetailPreview() {
    MaterialTheme {
        VentaDetailContent(state = VentaDetailState.Loaded(ventaEjemplo))
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Suppress("ktlint:standard:function-naming")
@Composable
fun VentaDetailLoadingPreview() {
    MaterialTheme {
        VentaDetailContent(state = VentaDetailState.Loading)
    }
}
