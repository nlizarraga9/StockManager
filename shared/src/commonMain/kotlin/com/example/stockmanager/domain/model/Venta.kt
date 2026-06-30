package com.example.stockmanager.domain.model

data class Venta(
    val id: String = "",
    val items: List<VentaItem>,
    val total: Double,
    val createdAt: String? = null,
) {
    companion object {
        fun calcularTotal(items: List<VentaItem>): Double = items.sumOf { it.subtotal }
    }
}
