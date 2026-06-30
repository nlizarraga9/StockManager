package com.example.stockmanager.domain.repository

import com.example.stockmanager.domain.model.Venta

interface VentaRepository {
    suspend fun crearVenta(venta: Venta): Venta

    suspend fun getVentas(): List<Venta>

    suspend fun getVenta(id: String): Venta

    suspend fun deleteVenta(id: String)
}
