package com.example.stockmanager.presentation.productos.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.stockmanager.domain.model.Producto
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import stockmanager.shared.generated.resources.Res
import stockmanager.shared.generated.resources.arrow_back

@Suppress("ktlint:standard:function-naming")
@Composable
fun ProductoFormScreen(
    navController: NavController,
    productoId: String?,
) {
    val viewModel = koinViewModel<ProductoFormViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    val esEdicion = productoId != null
    val snackbarHostState = remember { SnackbarHostState() }

    // Campos
    var nombre by rememberSaveable { mutableStateOf("") }
    var descripcion by rememberSaveable { mutableStateOf("") }
    var precio by rememberSaveable { mutableStateOf("") }
    var stock by rememberSaveable { mutableStateOf("") }
    var stockMinimo by rememberSaveable { mutableStateOf("5") }
    var camposCargados by rememberSaveable { mutableStateOf(false) }

    // Cargar datos is es edicion
    LaunchedEffect(productoId) {
        if (esEdicion && !camposCargados) {
            viewModel.cargarProducto(productoId!!)
        }
    }

    LaunchedEffect(state) {
        when (val s = state) {
            is ProductoFormState.Loaded -> {
                if (!camposCargados) {
                    nombre = s.producto.nombre
                    descripcion = s.producto.descripcion ?: ""
                    precio = s.producto.precio.toString()
                    stock = s.producto.stock.toString()
                    stockMinimo = s.producto.stockMinimo.toString()
                    camposCargados = true
                }
            }

            is ProductoFormState.SaveSuccess -> {
                navController.popBackStack()
            }

            is ProductoFormState.Error -> {
                snackbarHostState.showSnackbar(s.mensaje)
                viewModel.resetError()
            }

            else -> {
                Unit
            }
        }
    }

    ProductoFormContent(
        esEdicion = esEdicion,
        isSaving = state is ProductoFormState.Saving,
        isLoading = state is ProductoFormState.Loading,
        nombre = nombre,
        descripcion = descripcion,
        precio = precio,
        stock = stock,
        stockMinimo = stockMinimo,
        onNombreChange = { nombre = it },
        onDescripcionChange = { descripcion = it },
        onPrecioChange = { precio = it },
        onStockChange = { stock = it },
        onStockMinimoChange = { stockMinimo = it },
        onGuardar = {
            viewModel.guardarProducto(
                id = productoId,
                nombre = nombre,
                descripcion = descripcion,
                precio = precio,
                stock = stock,
                stockMinimo = stockMinimo,
            )
        },
        onBack = { navController.popBackStack() },
        snackbarHostState = snackbarHostState,
    )
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun ProductoFormContent(
    esEdicion: Boolean,
    isSaving: Boolean,
    isLoading: Boolean,
    nombre: String,
    descripcion: String,
    precio: String,
    stock: String,
    stockMinimo: String,
    onNombreChange: (String) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onPrecioChange: (String) -> Unit,
    onStockChange: (String) -> Unit,
    onStockMinimoChange: (String) -> Unit,
    onGuardar: () -> Unit,
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (esEdicion) "Editar producto" else "Nuevo producto") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(Res.drawable.arrow_back),
                            contentDescription = "Volver",
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState())
                        .imePadding(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Spacer(Modifier.height(4.dp))

                OutlinedTextField(
                    value = nombre,
                    onValueChange = onNombreChange,
                    label = { Text("Nombre *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isSaving,
                )

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = onDescripcionChange,
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    enabled = !isSaving,
                )

                OutlinedTextField(
                    value = precio,
                    onValueChange = onPrecioChange,
                    label = { Text("Precio *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    prefix = { Text("$ ") },
                    enabled = !isSaving,
                )

                OutlinedTextField(
                    value = stock,
                    onValueChange = onStockChange,
                    label = { Text("Stock actual *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    enabled = !isSaving,
                )

                OutlinedTextField(
                    value = stockMinimo,
                    onValueChange = onStockMinimoChange,
                    label = { Text("Stock mínimo") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    supportingText = { Text("Alerta de stock bajo cuando el stock llega a este valor") },
                    enabled = !isSaving,
                )

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = onGuardar,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSaving,
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Text(if (esEdicion) "Guardar cambios" else "Crear producto")
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

// ---- PREVIEWS ----

private val productoEjemplo = Producto(
    id = "1",
    nombre = "Coca Cola 2.25L",
    descripcion = "Gaseosa cola",
    precio = 1500.0,
    stock = 24,
    stockMinimo = 5,
)

@Preview(showBackground = true, showSystemUi = true)
@Suppress("ktlint:standard:function-naming")
@Composable
fun ProductoFormNuevoPreview() {
    MaterialTheme {
        ProductoFormContent(
            esEdicion = false,
            isSaving = false,
            isLoading = false,
            nombre = "",
            descripcion = "",
            precio = "",
            stock = "",
            stockMinimo = "5",
            onNombreChange = {},
            onDescripcionChange = {},
            onPrecioChange = {},
            onStockChange = {},
            onStockMinimoChange = {},
            onGuardar = {},
            onBack = {},
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Suppress("ktlint:standard:function-naming")
@Composable
fun ProductoFormEditarPreview() {
    MaterialTheme {
        ProductoFormContent(
            esEdicion = true,
            isSaving = false,
            isLoading = false,
            nombre = productoEjemplo.nombre,
            descripcion = productoEjemplo.descripcion ?: "",
            precio = productoEjemplo.precio.toString(),
            stock = productoEjemplo.stock.toString(),
            stockMinimo = productoEjemplo.stockMinimo.toString(),
            onNombreChange = {},
            onDescripcionChange = {},
            onPrecioChange = {},
            onStockChange = {},
            onStockMinimoChange = {},
            onGuardar = {},
            onBack = {},
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Suppress("ktlint:standard:function-naming")
@Composable
fun ProductoFormGuardandoPreview() {
    MaterialTheme {
        ProductoFormContent(
            esEdicion = true,
            isSaving = true,
            isLoading = false,
            nombre = productoEjemplo.nombre,
            descripcion = productoEjemplo.descripcion ?: "",
            precio = productoEjemplo.precio.toString(),
            stock = productoEjemplo.stock.toString(),
            stockMinimo = productoEjemplo.stockMinimo.toString(),
            onNombreChange = {},
            onDescripcionChange = {},
            onPrecioChange = {},
            onStockChange = {},
            onStockMinimoChange = {},
            onGuardar = {},
            onBack = {},
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Suppress("ktlint:standard:function-naming")
@Composable
fun ProductoFormLoadingPreview() {
    MaterialTheme {
        ProductoFormContent(
            esEdicion = true,
            isSaving = false,
            isLoading = true,
            nombre = "",
            descripcion = "",
            precio = "",
            stock = "",
            stockMinimo = "5",
            onNombreChange = {},
            onDescripcionChange = {},
            onPrecioChange = {},
            onStockChange = {},
            onStockMinimoChange = {},
            onGuardar = {},
            onBack = {},
        )
    }
}