package com.example.stockmanager.domain.repository

import com.example.stockmanager.domain.model.Producto

open class FakeProductoRepository(
    initialProductos: List<Producto> = emptyList()
) : ProductoRepository {
    val productos = initialProductos.toMutableList()

    override suspend fun getProductos(): List<Producto> {
        return productos
    }

    override suspend fun getProducto(id: String): Producto {
        return productos.find { it.id == id }
            ?: throw NoSuchElementException("Producto no encontrado: $id")
    }

    override suspend fun insertProducto(producto: Producto) {
        val newProducto = if (producto.id.isEmpty()) {
            producto.copy(id = "prod_${productos.size + 1}")
        } else {
            producto
        }
        productos.add(newProducto)
    }

    override suspend fun updateProducto(producto: Producto) {
        val index = productos.indexOfFirst { it.id == producto.id }
        if (index >= 0) {
            productos[index] = producto
        } else {
            throw NoSuchElementException("Producto no encontrado para actualizar: ${producto.id}")
        }
    }

    override suspend fun deleteProducto(id: String) {
        productos.removeAll { it.id == id }
    }
}
