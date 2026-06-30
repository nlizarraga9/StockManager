package com.example.stockmanager.presentation.venta.lista

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.stockmanager.domain.model.Venta
import com.example.stockmanager.domain.model.VentaItem
import com.example.stockmanager.utils.toFechaLegible
import com.example.stockmanager.utils.toPrice
import org.koin.compose.viewmodel.koinViewModel

@Suppress("ktlint:standard:function-naming")
@Composable
fun VentaListScreen(navController: NavController) {
    val viewModel = koinViewModel<VentaListViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    VentaListContent(
        state = state,
        onRetry = { viewModel.cargarVentas() },
        onVentaClick = { id -> navController.navigate("venta/$id/detalle") },
    )
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun VentaListContent(
    state: VentaListState,
    onRetry: () -> Unit = {},
    onVentaClick: (String) -> Unit = {},
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Historial de ventas") })
        },
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            when (val s = state) {
                is VentaListState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is VentaListState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("Ocurrio un error", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = s.mensaje,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = onRetry) { Text("Reintentar") }
                    }
                }

                is VentaListState.Empty -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("Todavía no hay ventas", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Las ventas que confirmes van a aparecer acá",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                is VentaListState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
                    ) {
                        items(s.ventas, key = { it.id }) { venta ->
                            VentaCard(venta = venta, onClick = { onVentaClick(venta.id) })
                        }
                    }
                }
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun VentaCard(
    venta: Venta,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = venta.createdAt.toFechaLegible(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(2.dp))
                val cantItems = venta.items.size
                Text(
                    text = if (cantItems == 1) "1 producto" else "$cantItems productos",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = venta.total.toPrice(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

// ---- PREVIEWS ----

private val ventasEjemplo =
    listOf(
        Venta(
            id = "1",
            total = 3000.0,
            createdAt = "2026-06-30T13:45:12.000000+00:00",
            items =
                listOf(
                    VentaItem(
                        productoId = "1",
                        nombreProducto = "Coca Cola 2.25L",
                        cantidad = 2,
                        precioUnitario = 1500.0,
                    ),
                ),
        ),
        Venta(
            id = "2",
            total = 2400.0,
            createdAt = "2026-06-29T10:12:00.000000+00:00",
            items =
                listOf(
                    VentaItem(productoId = "2", nombreProducto = "Arroz Gallo 1kg", cantidad = 1, precioUnitario = 900.0),
                    VentaItem(productoId = "3", nombreProducto = "Fideos Matarazzo 500g", cantidad = 2, precioUnitario = 750.0),
                ),
        ),
    )

@Preview(showBackground = true, showSystemUi = true)
@Suppress("ktlint:standard:function-naming")
@Composable
fun VentaListSuccessPreview() {
    MaterialTheme {
        VentaListContent(state = VentaListState.Success(ventasEjemplo))
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Suppress("ktlint:standard:function-naming")
@Composable
fun VentaListEmptyPreview() {
    MaterialTheme {
        VentaListContent(state = VentaListState.Empty)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Suppress("ktlint:standard:function-naming")
@Composable
fun VentaListLoadingPreview() {
    MaterialTheme {
        VentaListContent(state = VentaListState.Loading)
    }
}
