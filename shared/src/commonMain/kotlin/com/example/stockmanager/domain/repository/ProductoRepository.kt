package com.example.stockmanager.domain.repository

import com.example.stockmanager.domain.model.Producto

interface ProductoRepository {
    suspend fun getProductos(): List<Producto>

    suspend fun getProducto(id: String): Producto

    suspend fun insertProducto(producto: Producto)

    suspend fun deleteProducto(id: String)

    suspend fun updateProducto(producto: Producto)
}
