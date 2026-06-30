package com.example.stockmanager.data.remote

import com.example.stockmanager.domain.model.Venta
import com.example.stockmanager.domain.model.VentaItem
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VentaDto(
    val id: String = "",
    val total: Double,
    @SerialName("created_at")
    val createdAt: String? = null,
)

fun VentaDto.toDomain(items: List<VentaItem> = emptyList()): Venta =
    Venta(
        id = id,
        total = total,
        items = items,
        createdAt = createdAt,
    )

fun Venta.toDto(): VentaDto =
    VentaDto(
        id = id,
        total = total,
    )
