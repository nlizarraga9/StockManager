package com.example.stockmanager.domain.usecase.venta

import com.example.stockmanager.domain.model.Producto
import com.example.stockmanager.domain.model.Venta
import com.example.stockmanager.domain.model.VentaItem
import com.example.stockmanager.domain.repository.FakeProductoRepository
import com.example.stockmanager.domain.repository.FakeVentaRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EliminarVentaUseCaseTest {

    private val productoRepository = FakeProductoRepository()
    private val ventaRepository = FakeVentaRepository()
    private val eliminarVentaUseCase = EliminarVentaUseCase(ventaRepository, productoRepository)

    @Test
    fun `eliminar venta exitosa reabastece stock`() = runTest {
        // Arrange
        val producto1 = Producto(id = "p1", nombre = "Prod 1", precio = 10.0, stock = 5, stockMinimo = 2)
        val producto2 = Producto(id = "p2", nombre = "Prod 2", precio = 20.0, stock = 3, stockMinimo = 1)
        productoRepository.insertProducto(producto1)
        productoRepository.insertProducto(producto2)

        val venta = Venta(
            id = "v1",
            items = listOf(
                VentaItem(productoId = "p1", nombreProducto = "Prod 1", cantidad = 2, precioUnitario = 10.0),
                VentaItem(productoId = "p2", nombreProducto = "Prod 2", cantidad = 3, precioUnitario = 20.0)
            ),
            total = 80.0
        )
        ventaRepository.crearVenta(venta)

        // Act
        eliminarVentaUseCase("v1")

        // Assert
        // Verificar reabastecimiento de stock (p1: 5 + 2 = 7, p2: 3 + 3 = 6)
        assertEquals(7, productoRepository.getProducto("p1").stock)
        assertEquals(6, productoRepository.getProducto("p2").stock)

        // Verificar que la venta fue eliminada
        assertTrue(ventaRepository.ventas.isEmpty())
    }

    @Test
    fun `eliminar venta con producto eliminado es resiliente`() = runTest {
        // Arrange
        val producto1 = Producto(id = "p1", nombre = "Prod 1", precio = 10.0, stock = 5, stockMinimo = 2)
        productoRepository.insertProducto(producto1)
        // No insertamos el producto 2 en el repositorio para simular que fue eliminado

        val venta = Venta(
            id = "v1",
            items = listOf(
                VentaItem(productoId = "p1", nombreProducto = "Prod 1", cantidad = 2, precioUnitario = 10.0),
                VentaItem(productoId = "p2", nombreProducto = "Prod 2", cantidad = 3, precioUnitario = 20.0)
            ),
            total = 80.0
        )
        ventaRepository.crearVenta(venta)

        // Act
        eliminarVentaUseCase("v1")

        // Assert
        // Verificar que p1 reabasteció su stock normalmente
        assertEquals(7, productoRepository.getProducto("p1").stock)

        // Verificar que la venta fue eliminada sin lanzar excepciones por p2 inexistente
        assertTrue(ventaRepository.ventas.isEmpty())
    }
}
