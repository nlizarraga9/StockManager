package com.example.stockmanager.domain.model

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ProductoTest {

    @Test
    fun `producto sin stock`() {
        val producto = Producto(
            id = "1",
            nombre = "Martillo",
            precio = 15.0,
            stock = 0,
            stockMinimo = 5
        )
        assertTrue(producto.sinStock)
        assertFalse(producto.stockBajo)
    }

    @Test
    fun `producto con stock bajo en limite inferior`() {
        val producto = Producto(
            id = "1",
            nombre = "Martillo",
            precio = 15.0,
            stock = 1,
            stockMinimo = 5
        )
        assertFalse(producto.sinStock)
        assertTrue(producto.stockBajo)
    }

    @Test
    fun `producto con stock bajo en limite superior`() {
        val producto = Producto(
            id = "1",
            nombre = "Martillo",
            precio = 15.0,
            stock = 5,
            stockMinimo = 5
        )
        assertFalse(producto.sinStock)
        assertTrue(producto.stockBajo)
    }

    @Test
    fun `producto con stock normal`() {
        val producto = Producto(
            id = "1",
            nombre = "Martillo",
            precio = 15.0,
            stock = 6,
            stockMinimo = 5
        )
        assertFalse(producto.sinStock)
        assertFalse(producto.stockBajo)
    }
}
