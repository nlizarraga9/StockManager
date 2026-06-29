package com.example.stockmanager.data.repository

import com.example.stockmanager.data.remote.ProductoDto
import com.example.stockmanager.data.remote.SupabaseClientProvider
import com.example.stockmanager.data.remote.toDomain
import com.example.stockmanager.data.remote.toDto
import com.example.stockmanager.domain.model.Producto
import com.example.stockmanager.domain.repository.ProductoRepository
import io.github.jan.supabase.postgrest.from

class ProductoRepositoryImpl : ProductoRepository {
    private val client = SupabaseClientProvider.client

    override suspend fun getProductos(): List<Producto> =
        client
            .from("productos")
            .select()
            .decodeList<ProductoDto>()
            .map { it.toDomain() }

    override suspend fun getProducto(id: String): Producto =
        client
            .from("productos")
            .select { filter { eq("id", id) } }
            .decodeSingle<ProductoDto>()
            .toDomain()

    override suspend fun insertProducto(producto: Producto) {
        client
            .from("productos")
            .update(producto.toDto()) { filter { eq("id", producto.id) } }
    }

    override suspend fun updateProducto(producto: Producto) {
        client
            .from("productos")
            .update(producto.toDto()) { filter { eq("id", producto.id) } }
    }

    override suspend fun deleteProducto(id: String) {
        client
            .from("productos")
            .delete { filter { eq("id", id) } }
    }
}
