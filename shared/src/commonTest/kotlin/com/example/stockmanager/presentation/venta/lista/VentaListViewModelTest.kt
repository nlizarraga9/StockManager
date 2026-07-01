package com.example.stockmanager.presentation.venta.lista

import com.example.stockmanager.domain.model.Producto
import com.example.stockmanager.domain.model.Venta
import com.example.stockmanager.domain.model.VentaItem
import com.example.stockmanager.domain.repository.FakeProductoRepository
import com.example.stockmanager.domain.repository.FakeVentaRepository
import com.example.stockmanager.domain.usecase.venta.CrearVentaUseCase
import com.example.stockmanager.presentation.BaseViewModelTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VentaListViewModelTest : BaseViewModelTest() {

    private val ventaRepository = FakeVentaRepository()
    private val productoRepository = FakeProductoRepository()
    private val crearVenta = CrearVentaUseCase(ventaRepository, productoRepository)
    private val viewModel by lazy { VentaListViewModel(ventaRepository, crearVenta) }

    @Test
    fun `cargar ventas al iniciar ordena por fecha descendente`() {
        // Arrange
        val v1 = Venta(id = "v1", items = emptyList(), total = 50.0, createdAt = "2026-07-01T10:00:00Z")
        val v2 = Venta(id = "v2", items = emptyList(), total = 120.0, createdAt = "2026-07-01T11:00:00Z")
        ventaRepository.ventas.addAll(listOf(v1, v2))

        // Act & Assert (lazy viewModel triggers load on init)
        val state = viewModel.state.value
        assertTrue(state is VentaListState.Success)
        assertEquals(2, state.ventas.size)
        // Descending order (v2 is newer than v1)
        assertEquals("v2", state.ventas[0].id)
        assertEquals("v1", state.ventas[1].id)
    }

    @Test
    fun `cargar ventas vacio da estado empty`() {
        // Act & Assert
        assertEquals(VentaListState.Empty, viewModel.state.value)
    }

    @Test
    fun `cargar ventas fallido da estado error`() {
        // Arrange
        val brokenRepository = object : FakeVentaRepository() {
            override suspend fun getVentas(): List<Venta> {
                throw RuntimeException("Network timeout")
            }
        }
        val brokenViewModel = VentaListViewModel(brokenRepository, crearVenta)

        // Act & Assert
        val state = brokenViewModel.state.value
        assertTrue(state is VentaListState.Error)
        assertEquals("Network timeout", state.mensaje)
    }

    @Test
    fun `deshacer eliminar venta crea de nuevo la venta y recarga lista`() {
        // Arrange
        val prod = Producto(id = "p1", nombre = "Martillo", precio = 15.0, stock = 10)
        productoRepository.productos.add(prod)

        val items = listOf(
            VentaItem(productoId = "p1", nombreProducto = "Martillo", cantidad = 2, precioUnitario = 15.0)
        )

        // Act
        viewModel.deshacerEliminar(items)

        // Assert
        assertEquals(1, ventaRepository.ventas.size)
        assertEquals(8, productoRepository.productos.first().stock) // Se descontó stock al crear de nuevo
        assertTrue(viewModel.state.value is VentaListState.Success)
    }
}
