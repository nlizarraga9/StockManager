package com.example.stockmanager.data.repository

import com.example.stockmanager.data.remote.SupabaseClientProvider
import com.example.stockmanager.data.remote.VentaDto
import com.example.stockmanager.data.remote.VentaItemDto
import com.example.stockmanager.data.remote.toDomain
import com.example.stockmanager.data.remote.toDto
import com.example.stockmanager.domain.model.Venta
import com.example.stockmanager.domain.repository.VentaRepository
import io.github.jan.supabase.postgrest.from

class VentaRepositoryImpl : VentaRepository {
    private val client = SupabaseClientProvider.client

    override suspend fun crearVenta(venta: Venta): Venta {
        // Inserta venta y obtiene id generado por Supabase
        val ventaCreada =
            client
                .from("ventas")
                .insert(venta.toDto()) { select() }
                .decodeSingle<VentaDto>()

        // Inserta cada item con el ventaId obtenido
        val itemDtos = venta.items.map { it.toDto(ventaIdOverride = ventaCreada.id) }
        val itemsCreados =
            client
                .from("venta_items")
                .insert(itemDtos) { select() }
                .decodeList<VentaItemDto>()

        return ventaCreada.toDomain(items = itemsCreados.map { it.toDomain() })
    }

    override suspend fun getVentas(): List<Venta> {
        val ventas =
            client
                .from("ventas")
                .select()
                .decodeList<VentaDto>()

        return ventas.map { ventaDto ->
            val items =
                client
                    .from("venta_items")
                    .select { filter { eq("venta_id", ventaDto.id) } }
                    .decodeList<VentaItemDto>()
                    .map { it.toDomain() }
            ventaDto.toDomain(items = items)
        }
    }

    override suspend fun getVenta(id: String): Venta {
        val ventaDto =
            client
                .from("venta_items")
                .select { filter { eq("id", id) } }
                .decodeSingle<VentaDto>()

        val items =
            client
                .from("venta_items")
                .select { filter { eq("venta_id", id) } }
                .decodeList<VentaItemDto>()
                .map { it.toDomain() }

        return ventaDto.toDomain(items = items)
    }

    override suspend fun deleteVenta(id: String) {
        client
            .from("venta_items")
            .delete { filter { eq("vent_id", id) } }

        client
            .from("ventas")
            .delete { filter { eq("id", id) } }
    }
}
