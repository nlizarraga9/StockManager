package com.example.stockmanager.presentation.venta.nueva

import com.example.stockmanager.domain.model.Producto
import com.example.stockmanager.domain.repository.FakeProductoRepository
import com.example.stockmanager.domain.repository.FakeVentaRepository
import com.example.stockmanager.domain.usecase.producto.GetProductosUseCase
import com.example.stockmanager.domain.usecase.venta.CrearVentaUseCase
import com.example.stockmanager.presentation.BaseViewModelTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VentaViewModelTest : BaseViewModelTest() {

    private val productoRepository = FakeProductoRepository()
    private val ventaRepository = FakeVentaRepository()
    private val getProductos = GetProductosUseCase(productoRepository)
    private val crearVenta = CrearVentaUseCase(ventaRepository, productoRepository)

    private val viewModel by lazy { VentaViewModel(getProductos, crearVenta) }

    @Test
    fun `cargar productos al iniciar`() {
        // Arrange
        val producto1 = Producto(id = "p1", nombre = "Clavos", precio = 1.5, stock = 100)
        val producto2 = Producto(id = "p2", nombre = "Pintura", precio = 25.0, stock = 10)
        productoRepository.productos.add(producto1)
        productoRepository.productos.add(producto2)

        // Act & Assert (viewModel is initialized lazily, calling it triggers init)
        val state = viewModel.state.value
        assertTrue(state is VentaState.Idle)
        assertEquals(2, state.productos.size)
        assertEquals(producto1, state.productos[0])
        assertEquals(producto2, state.productos[1])
        assertTrue(state.itemsCarrito.isEmpty())
    }

    @Test
    fun `agregar al carrito producto nuevo y producto existente`() {
        // Arrange
        val producto = Producto(id = "p1", nombre = "Martillo", precio = 15.0, stock = 10)
        productoRepository.productos.add(producto)

        // Act - Agregar nuevo
        viewModel.agregarAlCarrito(producto, 2)

        // Assert
        var state = viewModel.state.value as VentaState.Idle
        assertEquals(1, state.itemsCarrito.size)
        assertEquals("p1", state.itemsCarrito[0].productoId)
        assertEquals(2, state.itemsCarrito[0].cantidad)
        assertEquals(15.0, state.itemsCarrito[0].precioUnitario)

        // Act - Agregar existente (debe acumular cantidad)
        viewModel.agregarAlCarrito(producto, 3)

        // Assert
        state = viewModel.state.value as VentaState.Idle
        assertEquals(1, state.itemsCarrito.size)
        assertEquals(5, state.itemsCarrito[0].cantidad)
    }

    @Test
    fun `quitar producto del carrito`() {
        // Arrange
        val producto = Producto(id = "p1", nombre = "Martillo", precio = 15.0, stock = 10)
        productoRepository.productos.add(producto)
        viewModel.agregarAlCarrito(producto, 2)

        // Act
        viewModel.quitarDelCarrito("p1")

        // Assert
        val state = viewModel.state.value as VentaState.Idle
        assertTrue(state.itemsCarrito.isEmpty())
    }

    @Test
    fun `cambiar cantidad valida y eliminar si es cero o menor`() {
        // Arrange
        val producto = Producto(id = "p1", nombre = "Martillo", precio = 15.0, stock = 10)
        productoRepository.productos.add(producto)
        viewModel.agregarAlCarrito(producto, 2)

        // Act - Cambiar a cantidad positiva
        viewModel.cambiarCantidad("p1", 8)

        // Assert
        var state = viewModel.state.value as VentaState.Idle
        assertEquals(8, state.itemsCarrito[0].cantidad)

        // Act - Cambiar a cantidad <= 0 (debe remover del carrito)
        viewModel.cambiarCantidad("p1", 0)

        // Assert
        state = viewModel.state.value as VentaState.Idle
        assertTrue(state.itemsCarrito.isEmpty())
    }

    @Test
    fun `intentar confirmar venta con carrito vacio lanza error`() {
        // Act
        viewModel.confirmarVenta()

        // Assert
        val state = viewModel.state.value
        assertTrue(state is VentaState.Error)
        assertEquals("Agregá al menos un producto a la venta", state.mensaje)
    }

    @Test
    fun `confirmar venta de forma exitosa`() {
        // Arrange
        val producto = Producto(id = "p1", nombre = "Martillo", precio = 15.0, stock = 10)
        productoRepository.productos.add(producto)
        viewModel.agregarAlCarrito(producto, 3)

        // Act
        viewModel.confirmarVenta()

        // Assert
        assertEquals(VentaState.VentaExitosa, viewModel.state.value)

        // Verificar descuento de stock en repositorio
        assertEquals(7, productoRepository.productos.first().stock)

        // Verificar registro de venta en repositorio
        assertEquals(1, ventaRepository.ventas.size)
        assertEquals(45.0, ventaRepository.ventas.first().total)
    }

    @Test
    fun `confirmar venta fallida por stock insuficiente`() {
        // Arrange
        val producto = Producto(id = "p1", nombre = "Martillo", precio = 15.0, stock = 2)
        productoRepository.productos.add(producto)
        viewModel.agregarAlCarrito(producto, 5)

        // Act
        viewModel.confirmarVenta()

        // Assert
        val state = viewModel.state.value
        assertTrue(state is VentaState.Error)
        assertTrue(state.mensaje.contains("Stock insuficiente"))

        // Verificar que el stock no cambió
        assertEquals(2, productoRepository.productos.first().stock)

        // Verificar que no se registró venta
        assertTrue(ventaRepository.ventas.isEmpty())
    }
}
