package com.example.stockmanager.domain.model

data class VentaItem(
    val id: String = "",
    val ventaId: String = "",
    val productoId: String,
    val nombreProducto: String,
    val cantidad: Int,
    val precioUnitario: Double,
) {
    val subtotal: Double get() = cantidad * precioUnitario
}
