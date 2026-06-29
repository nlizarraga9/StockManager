package com.example.stockmanager.domain.usecase

import com.example.stockmanager.domain.model.Producto
import com.example.stockmanager.domain.repository.ProductoRepository

class GetProductosUseCase(
    private val repository: ProductoRepository,
) {
    suspend operator fun invoke(): List<Producto> = repository.getProductos()
}
