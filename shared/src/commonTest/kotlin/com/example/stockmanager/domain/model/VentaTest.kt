package com.example.stockmanager.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class VentaTest {

    @Test
    fun `calcular total de venta con lista vacia`() {
        val total = Venta.calcularTotal(emptyList())
        assertEquals(0.0, total)
    }

    @Test
    fun `calcular total de venta con multiples items`() {
        val items = listOf(
            VentaItem(
                productoId = "p1",
                nombreProducto = "Prod 1",
                cantidad = 2,
                precioUnitario = 10.0
            ),
            VentaItem(
                productoId = "p2",
                nombreProducto = "Prod 2",
                cantidad = 1,
                precioUnitario = 25.5
            )
        )
        val total = Venta.calcularTotal(items)
        assertEquals(45.5, total)
    }
}
