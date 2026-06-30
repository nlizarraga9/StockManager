package com.example.stockmanager.data.remote

import com.example.stockmanager.domain.model.VentaItem
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VentaItemDto(
    val id: String = "",
    @SerialName("venta_id")
    val ventaId: String = "",
    @SerialName("producto_id")
    val productoId: String,
    @SerialName("nombre_producto")
    val nombreProducto: String,
    val cantidad: Int,
    @SerialName("precio_unitario")
    val precioUnitario: Double,
)

fun VentaItemDto.toDomain(): VentaItem =
    VentaItem(
        id = id,
        ventaId = ventaId,
        productoId = productoId,
        nombreProducto = nombreProducto,
        cantidad = cantidad,
        precioUnitario = precioUnitario,
    )

fun VentaItem.toDto(ventaIdOverride: String): VentaItemDto =
    VentaItemDto(
        ventaId = ventaIdOverride,
        productoId = productoId,
        nombreProducto = nombreProducto,
        cantidad = cantidad,
        precioUnitario = precioUnitario,
    )
