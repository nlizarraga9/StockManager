package com.example.stockmanager.data.remote

import com.example.stockmanager.domain.model.Producto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProductoDto(
    val id: String = "",
    val nombre: String,
    val descripcion: String? = null,
    val precio: Double,
    val stock: Int,
    @SerialName("stock_minimo")
    val stockMinimo: Int = 5,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null,
)

fun ProductoDto.toDomain(): Producto =
    Producto(
        id = id,
        nombre = nombre,
        descripcion = descripcion,
        precio = precio,
        stock = stock,
        stockMinimo = stockMinimo,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

fun Producto.toDto(): ProductoDto =
    ProductoDto(
        id = id,
        nombre = nombre,
        descripcion = descripcion,
        precio = precio,
        stock = stock,
        stockMinimo = stockMinimo,
    )
