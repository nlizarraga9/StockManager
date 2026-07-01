package com.example.stockmanager.domain.repository

import com.example.stockmanager.domain.model.Venta

open class FakeVentaRepository(
    initialVentas: List<Venta> = emptyList()
) : VentaRepository {
    val ventas = initialVentas.toMutableList()

    override suspend fun crearVenta(venta: Venta): Venta {
        val newVenta = if (venta.id.isEmpty()) {
            venta.copy(id = "venta_${ventas.size + 1}")
        } else {
            venta
        }
        ventas.add(newVenta)
        return newVenta
    }

    override suspend fun getVentas(): List<Venta> {
        return ventas
    }

    override suspend fun getVenta(id: String): Venta {
        return ventas.find { it.id == id }
            ?: throw NoSuchElementException("Venta no encontrada: $id")
    }

    override suspend fun deleteVenta(id: String) {
        ventas.removeAll { it.id == id }
    }
}
