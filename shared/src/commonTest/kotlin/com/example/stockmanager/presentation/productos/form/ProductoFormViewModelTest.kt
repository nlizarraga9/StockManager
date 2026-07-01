package com.example.stockmanager.presentation.productos.form

import com.example.stockmanager.domain.model.Producto
import com.example.stockmanager.domain.repository.FakeProductoRepository
import com.example.stockmanager.presentation.BaseViewModelTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProductoFormViewModelTest : BaseViewModelTest() {

    private val repository = FakeProductoRepository()
    private val viewModel by lazy { ProductoFormViewModel(repository) }

    @Test
    fun `estado inicial es idle`() {
        assertEquals(ProductoFormState.Idle, viewModel.state.value)
    }

    @Test
    fun `cargar producto de forma exitosa`() {
        // Arrange
        val producto = Producto(id = "p1", nombre = "Prod 1", precio = 10.0, stock = 5)
        repository.productos.add(producto)

        // Act
        viewModel.cargarProducto("p1")

        // Assert
        val state = viewModel.state.value
        assertTrue(state is ProductoFormState.Loaded)
        assertEquals(producto, state.producto)
    }

    @Test
    fun `cargar producto inexistente cambia a error`() {
        // Act
        viewModel.cargarProducto("p1")

        // Assert
        val state = viewModel.state.value
        assertTrue(state is ProductoFormState.Error)
        assertEquals("Producto no encontrado: p1", state.mensaje)
    }

    @Test
    fun `guardar producto con nombre vacio lanza error`() {
        // Act
        viewModel.guardarProducto(
            id = null,
            nombre = "",
            descripcion = "desc",
            precio = "10.0",
            stock = "5",
            stockMinimo = "2",
            imagenUrl = null
        )

        // Assert
        val state = viewModel.state.value
        assertTrue(state is ProductoFormState.Error)
        assertEquals("El nombre es obligatorio", state.mensaje)
    }

    @Test
    fun `guardar producto con precio invalido lanza error`() {
        // Act
        viewModel.guardarProducto(
            id = null,
            nombre = "Prod",
            descripcion = "desc",
            precio = "invalido",
            stock = "5",
            stockMinimo = "2",
            imagenUrl = null
        )

        // Assert
        var state = viewModel.state.value
        assertTrue(state is ProductoFormState.Error)
        assertEquals("El precio debe ser un número válido", state.mensaje)

        // Act con precio negativo
        viewModel.guardarProducto(
            id = null,
            nombre = "Prod",
            descripcion = "desc",
            precio = "-5.0",
            stock = "5",
            stockMinimo = "2",
            imagenUrl = null
        )

        // Assert
        state = viewModel.state.value
        assertTrue(state is ProductoFormState.Error)
        assertEquals("El precio debe ser un número válido", state.mensaje)
    }

    @Test
    fun `guardar producto con stock invalido lanza error`() {
        // Act
        viewModel.guardarProducto(
            id = null,
            nombre = "Prod",
            descripcion = "desc",
            precio = "10.0",
            stock = "invalido",
            stockMinimo = "2",
            imagenUrl = null
        )

        // Assert
        var state = viewModel.state.value
        assertTrue(state is ProductoFormState.Error)
        assertEquals("El stock debe ser un número válido", state.mensaje)

        // Act con stock negativo
        viewModel.guardarProducto(
            id = null,
            nombre = "Prod",
            descripcion = "desc",
            precio = "10.0",
            stock = "-2",
            stockMinimo = "2",
            imagenUrl = null
        )

        // Assert
        state = viewModel.state.value
        assertTrue(state is ProductoFormState.Error)
        assertEquals("El stock debe ser un número válido", state.mensaje)
    }

    @Test
    fun `insertar producto de forma exitosa`() {
        // Act
        viewModel.guardarProducto(
            id = null,
            nombre = "Nuevo Prod",
            descripcion = "Una desc",
            precio = "15.75",
            stock = "10",
            stockMinimo = "3",
            imagenUrl = "http://foto"
        )

        // Assert
        assertEquals(ProductoFormState.SaveSuccess, viewModel.state.value)
        assertEquals(1, repository.productos.size)
        val productoGuardado = repository.productos.first()
        assertEquals("Nuevo Prod", productoGuardado.nombre)
        assertEquals("Una desc", productoGuardado.descripcion)
        assertEquals(15.75, productoGuardado.precio)
        assertEquals(10, productoGuardado.stock)
        assertEquals(3, productoGuardado.stockMinimo)
        assertEquals("http://foto", productoGuardado.imagenUrl)
    }

    @Test
    fun `actualizar producto de forma exitosa`() {
        // Arrange
        val productoOriginal = Producto(
            id = "p1",
            nombre = "Original",
            descripcion = "original desc",
            precio = 10.0,
            stock = 5,
            stockMinimo = 2,
            imagenUrl = "url1"
        )
        repository.productos.add(productoOriginal)

        // Act (probando también reemplazo de coma por punto en precio)
        viewModel.guardarProducto(
            id = "p1",
            nombre = "Modificado",
            descripcion = "nueva desc",
            precio = "12,99",
            stock = "8",
            stockMinimo = "4",
            imagenUrl = "url2"
        )

        // Assert
        assertEquals(ProductoFormState.SaveSuccess, viewModel.state.value)
        assertEquals(1, repository.productos.size)
        val productoGuardado = repository.productos.first()
        assertEquals("p1", productoGuardado.id)
        assertEquals("Modificado", productoGuardado.nombre)
        assertEquals("nueva desc", productoGuardado.descripcion)
        assertEquals(12.99, productoGuardado.precio)
        assertEquals(8, productoGuardado.stock)
        assertEquals(4, productoGuardado.stockMinimo)
        assertEquals("url2", productoGuardado.imagenUrl)
    }

    @Test
    fun `resetear error cambia estado a idle`() {
        // Arrange
        viewModel.guardarProducto(null, "", "", "", "", "", null)
        assertTrue(viewModel.state.value is ProductoFormState.Error)

        // Act
        viewModel.resetError()

        // Assert
        assertEquals(ProductoFormState.Idle, viewModel.state.value)
    }
}
