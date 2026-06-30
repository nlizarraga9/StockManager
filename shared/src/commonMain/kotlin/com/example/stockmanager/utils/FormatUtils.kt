package com.example.stockmanager.utils

import kotlin.math.roundToInt

fun Double.toPrice(): String {
    val entero = this.toLong()
    val decimal = ((this - entero) * 100).roundToInt()
    return "$ $entero,${decimal.toString().padStart(2, '0')}"
}

private val nombresMes =
    listOf(
        "ene",
        "feb",
        "mar",
        "abr",
        "may",
        "jun",
        "jul",
        "ago",
        "sep",
        "oct",
        "nov",
        "dic",
    )

/**
 * Formatea un timestamp ISO-8601 (formato que devuelve Supabase, ej:
 * "2026-06-30T13:45:12.123456+00:00") a algo legible como "30 jun, 13:45".
 * Si el formato no es el esperado, devuelve el string original.
 */
fun String?.toFechaLegible(): String {
    if (this == null) return "Sin fecha"
    return try {
        val datePart = this.substringBefore("T")
        val timePart = this.substringAfter("T").take(5)
        val (anio, mes, dia) = datePart.split("-")
        val mesNombre = nombresMes.getOrElse(mes.toInt() - 1) { mes }
        "$dia $mesNombre, $timePart"
    } catch (e: Exception) {
        this
    }
}
