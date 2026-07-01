package com.example.stockmanager.domain.model

data class Producto(
    val id: String = "",
    val nombre: String,
    val descripcion: String? = null,
    val precio: Double,
    val stock: Int,
    val stockMinimo: Int = 5,
    val imagenUrl: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
) {
    val stockBajo: Boolean get() = stock in 1..stockMinimo
    val sinStock: Boolean get() = stock == 0
}
