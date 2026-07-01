package com.example.stockmanager.presentation.productos.form

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.foundation.clickable
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.unit.Dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.stockmanager.domain.model.Producto
import com.example.stockmanager.utils.Base64Factory
import com.example.stockmanager.utils.decodeBase64ToBitmap
import com.example.stockmanager.utils.rememberImagePicker
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import stockmanager.shared.generated.resources.Res
import stockmanager.shared.generated.resources.arrow_back
import stockmanager.shared.generated.resources.inventory
import stockmanager.shared.generated.resources.sin_imagen

@Suppress("ktlint:standard:function-naming")
@Composable
fun ProductoFormScreen(
    navController: NavController,
    productoId: String?,
    snackbarHostState: SnackbarHostState,
) {
    val viewModel = koinViewModel<ProductoFormViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    val esEdicion = productoId != null

    // Campos
    var nombre by rememberSaveable { mutableStateOf("") }
    var descripcion by rememberSaveable { mutableStateOf("") }
    var precio by rememberSaveable { mutableStateOf("") }
    var stock by rememberSaveable { mutableStateOf("") }
    var stockMinimo by rememberSaveable { mutableStateOf("5") }
    var imagenUrl by rememberSaveable { mutableStateOf<String?>(null) }
    var camposCargados by rememberSaveable { mutableStateOf(false) }

    // Cargar datos is es edicion
    LaunchedEffect(productoId) {
        if (productoId != null && !camposCargados) {
            viewModel.cargarProducto(productoId)
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
                    imagenUrl = s.producto.imagenUrl
                    camposCargados = true
                }
            }

            is ProductoFormState.SaveSuccess -> {
                navController.previousBackStackEntry?.savedStateHandle?.set(
                    "producto_action_success",
                    if (esEdicion) "edit" else "add"
                )
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
        imagenUrl = imagenUrl,
        onNombreChange = { nombre = it },
        onDescripcionChange = { descripcion = it },
        onPrecioChange = { precio = it },
        onStockChange = { stock = it },
        onStockMinimoChange = { stockMinimo = it },
        onImagenChange = { imagenUrl = it },
        onGuardar = {
            viewModel.guardarProducto(
                id = productoId,
                nombre = nombre,
                descripcion = descripcion,
                precio = precio,
                stock = stock,
                stockMinimo = stockMinimo,
                imagenUrl = imagenUrl,
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
    imagenUrl: String?,
    onNombreChange: (String) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onPrecioChange: (String) -> Unit,
    onStockChange: (String) -> Unit,
    onStockMinimoChange: (String) -> Unit,
    onImagenChange: (String?) -> Unit,
    onGuardar: () -> Unit,
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(if (esEdicion) "Editar producto" else "Nuevo producto") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(Res.drawable.arrow_back),
                            contentDescription = "Volver",
                            modifier = Modifier.size(28.dp),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
            )
        },
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

                // Sección de imagen
                Text(
                    text = "Foto del Producto (opcional)",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                val imagePicker =
                    rememberImagePicker { bytes ->
                        val base64 = "data:image/jpeg;base64," + Base64Factory.encode(bytes)
                        onImagenChange(base64)
                    }

                Card(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .then(
                                if (imagenUrl == null) {
                                    Modifier.dashedBorder(
                                        width = 1.5.dp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                        shape = RoundedCornerShape(12.dp),
                                    )
                                } else {
                                    Modifier
                                }
                            ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        val bitmap = imagenUrl?.decodeBase64ToBitmap()
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap,
                                contentDescription = "Imagen del producto",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                            )
                        } else {
                            // Mostrar default de sin imagen
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable(enabled = !isSaving) { imagePicker.pickImage() },
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Image(
                                    painter = painterResource(Res.drawable.sin_imagen),
                                    contentDescription = "Sin imagen",
                                    modifier = Modifier.size(60.dp),
                                    contentScale = ContentScale.Fit,
                                    alpha = 0.5f,
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "Tocá para agregar imagen",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                )
                            }
                        }

                        // Overlay píldora con opciones de imagen
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.9f),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(12.dp),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "Galería",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.clickable(enabled = !isSaving) { imagePicker.pickImage() },
                                )
                                Text(
                                    text = "|",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                )
                                Text(
                                    text = "Cámara",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.clickable(enabled = !isSaving) { imagePicker.takePhoto() },
                                )
                                if (imagenUrl != null) {
                                    Text(
                                        text = "|",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                    )
                                    Text(
                                        text = "Quitar",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.clickable(enabled = !isSaving) { onImagenChange(null) },
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))

                OutlinedTextField(
                    value = nombre,
                    onValueChange = onNombreChange,
                    label = { Text("Nombre *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isSaving,
                    shape = RoundedCornerShape(12.dp),
                )

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = onDescripcionChange,
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    enabled = !isSaving,
                    shape = RoundedCornerShape(12.dp),
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
                    shape = RoundedCornerShape(12.dp),
                )

                OutlinedTextField(
                    value = stock,
                    onValueChange = onStockChange,
                    label = { Text("Stock actual *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    enabled = !isSaving,
                    shape = RoundedCornerShape(12.dp),
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
                    shape = RoundedCornerShape(12.dp),
                )

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = onGuardar,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSaving,
                    shape = RoundedCornerShape(12.dp),
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

private val productoEjemplo =
    Producto(
        id = "1",
        nombre = "Coca Cola 2.25L",
        descripcion = "Gaseosa cola",
        precio = 1500.0,
        stock = 24,
        stockMinimo = 5,
        imagenUrl = null,
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
            imagenUrl = null,
            onNombreChange = {},
            onDescripcionChange = {},
            onPrecioChange = {},
            onStockChange = {},
            onStockMinimoChange = {},
            onImagenChange = {},
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
            imagenUrl = null,
            onNombreChange = {},
            onDescripcionChange = {},
            onPrecioChange = {},
            onStockChange = {},
            onStockMinimoChange = {},
            onImagenChange = {},
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
            imagenUrl = null,
            onNombreChange = {},
            onDescripcionChange = {},
            onPrecioChange = {},
            onStockChange = {},
            onStockMinimoChange = {},
            onImagenChange = {},
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
            imagenUrl = null,
            onNombreChange = {},
            onDescripcionChange = {},
            onPrecioChange = {},
            onStockChange = {},
            onStockMinimoChange = {},
            onImagenChange = {},
            onGuardar = {},
            onBack = {},
        )
    }
}

fun Modifier.dashedBorder(width: Dp, color: Color, shape: Shape): Modifier = this.drawWithContent {
    drawContent()
    val stroke = Stroke(
        width = width.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f),
    )
    val outline = shape.createOutline(size, layoutDirection, this)
    val path = Path().apply {
        addOutline(outline)
    }
    drawPath(path = path, color = color, style = stroke)
}
