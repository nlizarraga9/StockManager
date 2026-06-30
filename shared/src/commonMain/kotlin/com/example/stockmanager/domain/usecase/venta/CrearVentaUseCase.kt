package com.example.stockmanager.domain.usecase.venta

import com.example.stockmanager.domain.model.Venta
import com.example.stockmanager.domain.model.VentaItem
import com.example.stockmanager.domain.repository.ProductoRepository
import com.example.stockmanager.domain.repository.VentaRepository

class CrearVentaUseCase(
    private val ventaRepository: VentaRepository,
    private val productoRepository: ProductoRepository,
) {
    /*
     * Crea una venta y descuenta el stock de cada producto involucrado.
     * Lanza excepción si algún producto no tiene stock suficiente.
     */
    suspend operator fun invoke(items: List<VentaItem>): Venta {
        // Valida stock de todos los items
        items.forEach { item ->
            val producto = productoRepository.getProducto(item.productoId)
            if (producto.stock < item.cantidad) {
                throw IllegalStateException(
                    "Stock insuficiente para ${producto.nombre}. " +
                        "Disponible: ${producto.stock}, solicitado: ${item.cantidad}",
                )
            }
        }

        // Descuenta stock de cada producto
        items.forEach { item ->
            val producto = productoRepository.getProducto(item.productoId)
            productoRepository.updateProducto(
                producto.copy(stock = producto.stock - item.cantidad),
            )
        }

        val venta =
            Venta(
                items = items,
                total = Venta.Companion.calcularTotal(items),
            )
        return ventaRepository.crearVenta(venta)
    }
}
