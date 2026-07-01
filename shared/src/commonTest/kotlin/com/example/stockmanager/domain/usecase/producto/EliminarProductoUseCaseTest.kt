package com.example.stockmanager.domain.usecase.producto

import com.example.stockmanager.domain.model.Producto
import com.example.stockmanager.domain.repository.FakeProductoRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EliminarProductoUseCaseTest {

    private val repository = FakeProductoRepository()
    private val eliminarProductoUseCase = EliminarProductoUseCase(repository)

    @Test
    fun `eliminar producto de forma exitosa`() = runTest {
        // Arrange
        val producto = Producto(id = "p1", nombre = "Prod 1", precio = 10.0, stock = 5)
        repository.insertProducto(producto)
        assertEquals(1, repository.productos.size)

        // Act
        eliminarProductoUseCase("p1")

        // Assert
        assertTrue(repository.productos.isEmpty())
    }
}
