package com.example.stockmanager.domain.usecase.venta

import com.example.stockmanager.domain.model.Producto
import com.example.stockmanager.domain.model.VentaItem
import com.example.stockmanager.domain.repository.FakeProductoRepository
import com.example.stockmanager.domain.repository.FakeVentaRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CrearVentaUseCaseTest {

    private val productoRepository = FakeProductoRepository()
    private val ventaRepository = FakeVentaRepository()
    private val crearVentaUseCase = CrearVentaUseCase(ventaRepository, productoRepository)

    @Test
    fun `crear venta exitosa con stock suficiente`() = runTest {
        // Arrange
        val producto1 = Producto(id = "p1", nombre = "Prod 1", precio = 10.0, stock = 10, stockMinimo = 2)
        val producto2 = Producto(id = "p2", nombre = "Prod 2", precio = 20.0, stock = 5, stockMinimo = 1)
        productoRepository.insertProducto(producto1)
        productoRepository.insertProducto(producto2)

        val items = listOf(
            VentaItem(productoId = "p1", nombreProducto = "Prod 1", cantidad = 3, precioUnitario = 10.0),
            VentaItem(productoId = "p2", nombreProducto = "Prod 2", cantidad = 2, precioUnitario = 20.0)
        )

        // Act
        val venta = crearVentaUseCase(items)

        // Assert
        assertEquals(70.0, venta.total)
        assertEquals(2, venta.items.size)

        // Verificar descuento de stock
        assertEquals(7, productoRepository.getProducto("p1").stock)
        assertEquals(3, productoRepository.getProducto("p2").stock)

        // Verificar que la venta se guardó
        assertEquals(1, ventaRepository.ventas.size)
        assertEquals(venta, ventaRepository.ventas.first())
    }

    @Test
    fun `intentar crear venta con stock insuficiente lanza excepcion`() = runTest {
        // Arrange
        val producto = Producto(id = "p1", nombre = "Prod 1", precio = 10.0, stock = 2, stockMinimo = 1)
        productoRepository.insertProducto(producto)

        val items = listOf(
            VentaItem(productoId = "p1", nombreProducto = "Prod 1", cantidad = 5, precioUnitario = 10.0)
        )

        // Act & Assert
        assertFailsWith<IllegalStateException> {
            crearVentaUseCase(items)
        }

        // Verificar que el stock NO cambió
        assertEquals(2, productoRepository.getProducto("p1").stock)

        // Verificar que la venta NO se guardó
        assertTrue(ventaRepository.ventas.isEmpty())
    }
}
