package com.example.stockmanager.utils

import kotlin.math.roundToInt

fun Double.toPrice(): String {
    val entero = this.toLong()
    val decimal = ((this - entero) * 100).roundToInt()
    return "$ $entero,${decimal.toString().padStart(2, '0')}"
}
