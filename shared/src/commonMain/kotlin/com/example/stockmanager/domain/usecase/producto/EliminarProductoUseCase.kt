package com.example.stockmanager.domain.usecase.producto

import com.example.stockmanager.domain.repository.ProductoRepository

class EliminarProductoUseCase(
    private val repository: ProductoRepository,
) {
    suspend operator fun invoke(id: String) = repository.deleteProducto(id)
}
