package com.example.stockmanager.domain.usecase.producto

import com.example.stockmanager.domain.model.Producto
import com.example.stockmanager.domain.repository.FakeProductoRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetProductosUseCaseTest {

    private val repository = FakeProductoRepository()
    private val getProductosUseCase = GetProductosUseCase(repository)

    @Test
    fun `obtener lista de productos vacia`() = runTest {
        val productos = getProductosUseCase()
        assertTrue(productos.isEmpty())
    }

    @Test
    fun `obtener lista con productos registrados`() = runTest {
        val producto = Producto(id = "p1", nombre = "Prod 1", precio = 10.0, stock = 5)
        repository.insertProducto(producto)

        val productos = getProductosUseCase()
        assertEquals(1, productos.size)
        assertEquals(producto, productos.first())
    }
}
