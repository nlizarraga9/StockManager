package com.example.stockmanager.presentation.productos.detalle

import com.example.stockmanager.domain.model.Producto
import com.example.stockmanager.domain.repository.FakeProductoRepository
import com.example.stockmanager.domain.usecase.producto.EliminarProductoUseCase
import com.example.stockmanager.presentation.BaseViewModelTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProductoDetailViewModelTest : BaseViewModelTest() {

    private val repository = FakeProductoRepository()
    private val eliminarProducto = EliminarProductoUseCase(repository)
    private val viewModel by lazy { ProductoDetailViewModel(repository, eliminarProducto) }

    @Test
    fun `estado inicial es loading`() {
        assertEquals(ProductoDetailState.Loading, viewModel.state.value)
    }

    @Test
    fun `cargar producto exitoso`() {
        // Arrange
        val prod = Producto(id = "p1", nombre = "Tornillo", precio = 0.5, stock = 500)
        repository.productos.add(prod)

        // Act
        viewModel.cargarProducto("p1")

        // Assert
        val state = viewModel.state.value
        assertTrue(state is ProductoDetailState.Loaded)
        assertEquals(prod, state.producto)
    }

    @Test
    fun `cargar producto fallido da estado error`() {
        // Act
        viewModel.cargarProducto("inexistente")

        // Assert
        val state = viewModel.state.value
        assertTrue(state is ProductoDetailState.Error)
        assertEquals("Producto no encontrado: inexistente", state.mensaje)
    }

    @Test
    fun `eliminar producto cambia de estados exitosamente`() {
        // Arrange
        val prod = Producto(id = "p1", nombre = "Tornillo", precio = 0.5, stock = 500)
        repository.productos.add(prod)

        // Act
        viewModel.eliminar("p1")

        // Assert
        assertEquals(ProductoDetailState.Deleted, viewModel.state.value)
        assertTrue(repository.productos.isEmpty())
    }

    @Test
    fun `eliminar producto fallido da estado error`() {
        // Arrange
        val brokenRepository = object : FakeProductoRepository() {
            override suspend fun deleteProducto(id: String) {
                throw RuntimeException("Error de conexion")
            }
        }
        val brokenViewModel = ProductoDetailViewModel(brokenRepository, EliminarProductoUseCase(brokenRepository))

        // Act
        brokenViewModel.eliminar("p1")

        // Assert
        val state = brokenViewModel.state.value
        assertTrue(state is ProductoDetailState.Error)
        assertEquals("Error de conexion", state.mensaje)
    }

    @Test
    fun `resetear error cambia estado a loading`() {
        // Arrange
        viewModel.cargarProducto("inexistente")
        assertTrue(viewModel.state.value is ProductoDetailState.Error)

        // Act
        viewModel.resetError()

        // Assert
        assertEquals(ProductoDetailState.Loading, viewModel.state.value)
    }
}
