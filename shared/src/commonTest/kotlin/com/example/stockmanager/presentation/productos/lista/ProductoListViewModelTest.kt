package com.example.stockmanager.presentation.productos.lista

import com.example.stockmanager.domain.model.Producto
import com.example.stockmanager.domain.repository.FakeProductoRepository
import com.example.stockmanager.domain.usecase.producto.GetProductosUseCase
import com.example.stockmanager.presentation.BaseViewModelTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProductoListViewModelTest : BaseViewModelTest() {

    private val repository = FakeProductoRepository()
    private val getProductos = GetProductosUseCase(repository)
    private val viewModel by lazy { ProductoListViewModel(getProductos, repository) }

    @Test
    fun `estado inicial es loading`() {
        assertEquals(ProductoListState.Loading, viewModel.state.value)
    }

    @Test
    fun `cargar productos exitoso`() {
        // Arrange
        val prod1 = Producto(id = "1", nombre = "Martillo", precio = 10.0, stock = 5)
        repository.productos.add(prod1)

        // Act
        viewModel.cargarProductos()

        // Assert
        val state = viewModel.state.value
        assertTrue(state is ProductoListState.Success)
        assertEquals(1, state.productos.size)
        assertEquals(prod1, state.productos.first())
    }

    @Test
    fun `cargar productos vacio da estado empty`() {
        // Act
        viewModel.cargarProductos()

        // Assert
        assertEquals(ProductoListState.Empty, viewModel.state.value)
    }

    @Test
    fun `cargar productos fallido da estado error`() {
        // Arrange
        val brokenRepository = object : FakeProductoRepository() {
            override suspend fun getProductos(): List<Producto> {
                throw RuntimeException("Database error")
            }
        }
        val brokenViewModel = ProductoListViewModel(GetProductosUseCase(brokenRepository), brokenRepository)

        // Act
        brokenViewModel.cargarProductos()

        // Assert
        val state = brokenViewModel.state.value
        assertTrue(state is ProductoListState.Error)
        assertEquals("Database error", state.mensaje)
    }

    @Test
    fun `ordenar productos por diferentes modos`() {
        // Arrange
        val prodA = Producto(id = "1", nombre = "Serrucho", precio = 15.0, stock = 10)
        val prodB = Producto(id = "2", nombre = "Alicate", precio = 5.0, stock = 20)
        val prodC = Producto(id = "3", nombre = "Taladro", precio = 50.0, stock = 2)
        repository.productos.addAll(listOf(prodA, prodB, prodC))

        viewModel.cargarProductos()

        // Act & Assert - Orden Alfabetico (A-Z)
        viewModel.setSortMode(ProductSortMode.ALPHABETICAL)
        var state = viewModel.state.value as ProductoListState.Success
        assertEquals("Alicate", state.productos[0].nombre)
        assertEquals("Serrucho", state.productos[1].nombre)
        assertEquals("Taladro", state.productos[2].nombre)

        // Act & Assert - Orden Stock Ascendente
        viewModel.setSortMode(ProductSortMode.STOCK_ASC)
        state = viewModel.state.value as ProductoListState.Success
        assertEquals("Taladro", state.productos[0].nombre)  // stock 2
        assertEquals("Serrucho", state.productos[1].nombre) // stock 10
        assertEquals("Alicate", state.productos[2].nombre)  // stock 20

        // Act & Assert - Orden Stock Descendente
        viewModel.setSortMode(ProductSortMode.STOCK_DESC)
        state = viewModel.state.value as ProductoListState.Success
        assertEquals("Alicate", state.productos[0].nombre)  // stock 20
        assertEquals("Serrucho", state.productos[1].nombre) // stock 10
        assertEquals("Taladro", state.productos[2].nombre)  // stock 2
    }

    @Test
    fun `buscar y filtrar productos por nombre y descripcion`() {
        // Arrange
        val prod1 = Producto(id = "1", nombre = "Pintura Roja", descripcion = "Para exteriores", precio = 20.0, stock = 5)
        val prod2 = Producto(id = "2", nombre = "Pincel fino", descripcion = "Cerdas suaves", precio = 3.0, stock = 15)
        repository.productos.addAll(listOf(prod1, prod2))

        viewModel.cargarProductos()

        // Act - Buscar "roja"
        viewModel.setSearchQuery("roja")

        // Assert
        var state = viewModel.state.value as ProductoListState.Success
        assertEquals(1, state.productos.size)
        assertEquals("Pintura Roja", state.productos.first().nombre)

        // Act - Buscar "suaves" (debe encontrar en la descripción)
        viewModel.setSearchQuery("suaves")

        // Assert
        state = viewModel.state.value as ProductoListState.Success
        assertEquals(1, state.productos.size)
        assertEquals("Pincel fino", state.productos.first().nombre)
    }

    @Test
    fun `deshacer eliminar restaura el producto en la lista`() {
        // Arrange
        val prod = Producto(id = "p1", nombre = "Para borrar", precio = 10.0, stock = 5)
        // No lo agregamos al repo, simulando que ya se borró del mismo

        // Act
        viewModel.deshacerEliminar(prod)

        // Assert
        assertEquals(1, repository.productos.size)
        assertEquals(prod, repository.productos.first())

        val state = viewModel.state.value
        assertTrue(state is ProductoListState.Success)
        assertEquals(1, state.productos.size)
    }
}
