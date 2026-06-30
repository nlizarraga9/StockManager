package com.example.stockmanager.domain.usecase.venta

import com.example.stockmanager.domain.repository.ProductoRepository
import com.example.stockmanager.domain.repository.VentaRepository

class EliminarVentaUseCase(
    private val ventaRepository: VentaRepository,
    private val productoRepository: ProductoRepository,
) {
    /*
     * Elimina una venta y devuelve el stock de cada producto involucrado.
     * Si un producto fue eliminado luego de la venta, se ignora ese item
     * (no se puede devolver stock a algo que no existe).
     */
    suspend operator fun invoke(ventaId: String) {
        val venta = ventaRepository.getVenta(ventaId)

        venta.items.forEach { item ->
            runCatching {
                val producto = productoRepository.getProducto(item.productoId)
                productoRepository.updateProducto(producto.copy(stock = producto.stock + item.cantidad))
            }
        }

        ventaRepository.deleteVenta(ventaId)
    }
}
