package com.example.stockmanager.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class VentaItemTest {

    @Test
    fun `calcular subtotal con cantidad y precio unitario`() {
        val item = VentaItem(
            id = "1",
            productoId = "prod_1",
            nombreProducto = "Pala",
            cantidad = 3,
            precioUnitario = 15.5
        )
        assertEquals(46.5, item.subtotal)
    }

    @Test
    fun `calcular subtotal con cantidad cero`() {
        val item = VentaItem(
            id = "1",
            productoId = "prod_1",
            nombreProducto = "Pala",
            cantidad = 0,
            precioUnitario = 10.0
        )
        assertEquals(0.0, item.subtotal)
    }
}
