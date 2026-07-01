package com.example.stockmanager.presentation.venta.detalle

import com.example.stockmanager.domain.model.Producto
import com.example.stockmanager.domain.model.Venta
import com.example.stockmanager.domain.model.VentaItem
import com.example.stockmanager.domain.repository.FakeProductoRepository
import com.example.stockmanager.domain.repository.FakeVentaRepository
import com.example.stockmanager.domain.usecase.venta.EliminarVentaUseCase
import com.example.stockmanager.presentation.BaseViewModelTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VentaDetailViewModelTest : BaseViewModelTest() {

    private val ventaRepository = FakeVentaRepository()
    private val productoRepository = FakeProductoRepository()
    private val eliminarVenta = EliminarVentaUseCase(ventaRepository, productoRepository)
    private val viewModel by lazy { VentaDetailViewModel(ventaRepository, eliminarVenta) }

    @Test
    fun `estado inicial es loading`() {
        assertEquals(VentaDetailState.Loading, viewModel.state.value)
    }

    @Test
    fun `cargar venta exitoso`() {
        // Arrange
        val venta = Venta(id = "v1", items = emptyList(), total = 50.0)
        ventaRepository.ventas.add(venta)

        // Act
        viewModel.cargarVenta("v1")

        // Assert
        val state = viewModel.state.value
        assertTrue(state is VentaDetailState.Loaded)
        assertEquals(venta, state.venta)
    }

    @Test
    fun `cargar venta fallido da estado error`() {
        // Act
        viewModel.cargarVenta("v1")

        // Assert
        val state = viewModel.state.value
        assertTrue(state is VentaDetailState.Error)
        assertEquals("Venta no encontrada: v1", state.mensaje)
    }

    @Test
    fun `eliminar venta exitoso reabastece stock y cambia a deleted`() {
        // Arrange
        val prod = Producto(id = "p1", nombre = "Martillo", precio = 15.0, stock = 10)
        productoRepository.productos.add(prod)

        val venta = Venta(
            id = "v1",
            items = listOf(VentaItem(productoId = "p1", nombreProducto = "Martillo", cantidad = 3, precioUnitario = 15.0)),
            total = 45.0
        )
        ventaRepository.ventas.add(venta)

        // Act
        viewModel.eliminar("v1")

        // Assert
        assertEquals(VentaDetailState.Deleted, viewModel.state.value)
        assertEquals(13, productoRepository.productos.first().stock) // Reabastecido 10 + 3 = 13
        assertTrue(ventaRepository.ventas.isEmpty())
    }

    @Test
    fun `eliminar venta fallido da estado error`() {
        // Arrange
        val brokenRepository = object : FakeVentaRepository() {
            override suspend fun getVenta(id: String): Venta {
                throw RuntimeException("Error al conectar")
            }
        }
        val brokenViewModel = VentaDetailViewModel(brokenRepository, EliminarVentaUseCase(brokenRepository, productoRepository))

        // Act
        brokenViewModel.eliminar("v1")

        // Assert
        val state = brokenViewModel.state.value
        assertTrue(state is VentaDetailState.Error)
        assertEquals("Error al conectar", state.mensaje)
    }

    @Test
    fun `resetear error cambia estado a loading`() {
        // Arrange
        viewModel.cargarVenta("inexistente")
        assertTrue(viewModel.state.value is VentaDetailState.Error)

        // Act
        viewModel.resetError()

        // Assert
        assertEquals(VentaDetailState.Loading, viewModel.state.value)
    }
}
