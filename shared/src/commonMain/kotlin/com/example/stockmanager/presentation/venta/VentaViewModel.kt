package com.example.stockmanager.presentation.venta

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockmanager.domain.model.Producto
import com.example.stockmanager.domain.model.Venta
import com.example.stockmanager.domain.model.VentaItem
import com.example.stockmanager.domain.usecase.producto.GetProductosUseCase
import com.example.stockmanager.domain.usecase.venta.CrearVentaUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class VentaState {
    data object LoadingProductos : VentaState()

    data class Idle(
        val productos: List<Producto>,
        val itemsCarrito: List<VentaItem> = emptyList(),
    ) : VentaState()

    data object Guardando : VentaState()

    data object VentaExitosa : VentaState()

    data class Error(
        val mensaje: String,
    ) : VentaState()
}

class VentaViewModel(
    private val getProductos: GetProductosUseCase,
    private val crearVenta: CrearVentaUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow<VentaState>(VentaState.LoadingProductos)
    val state: StateFlow<VentaState> = _state.asStateFlow()

    init {
        cargarProductos()
    }

    private fun cargarProductos() {
        viewModelScope.launch {
            _state.value = VentaState.LoadingProductos
            try {
                val productos = getProductos()
                _state.value = VentaState.Idle(productos = productos)
            } catch (e: Exception) {
                _state.value = VentaState.Error(e.message ?: "Error al cargar productos")
            }
        }
    }

    fun agregarAlCarrito(
        producto: Producto,
        cantidad: Int,
    ) {
        val current = _state.value as? VentaState.Idle ?: return
        val itemsActuales = current.itemsCarrito.toMutableList()
        val indexExistente = itemsActuales.indexOfFirst { it.productoId == producto.id }
        if (indexExistente >= 0) {
            val itemExistente = itemsActuales[indexExistente]
            itemsActuales[indexExistente] =
                itemExistente.copy(
                    cantidad = itemExistente.cantidad + cantidad,
                )
        } else {
            itemsActuales.add(
                VentaItem(
                    productoId = producto.id,
                    nombreProducto = producto.nombre,
                    cantidad = cantidad,
                    precioUnitario = producto.precio,
                ),
            )
        }
        _state.value = current.copy(itemsCarrito = itemsActuales)
    }

    fun quitarDelCarrito(productoId: String) {
        val current = _state.value as? VentaState.Idle ?: return
        _state.value =
            current.copy(
                itemsCarrito = current.itemsCarrito.filter { it.productoId != productoId },
            )
    }

    fun cambiarCantidad(
        productoId: String,
        nuevaCantidad: Int,
    ) {
        val current = _state.value as? VentaState.Idle ?: return
        if (nuevaCantidad <= 0) {
            quitarDelCarrito(productoId)
            return
        }
        _state.value =
            current.copy(
                itemsCarrito =
                    current.itemsCarrito.map {
                        if (it.productoId == productoId) it.copy(cantidad = nuevaCantidad) else it
                    },
            )
    }

    fun confirmarVenta() {
        val current = _state.value as? VentaState.Idle ?: return
        if (current.itemsCarrito.isEmpty()) {
            _state.value = VentaState.Error("Agregá al menos un producto a la venta")
            return
        }
        viewModelScope.launch {
            _state.value = VentaState.Guardando
            try {
                crearVenta(current.itemsCarrito)
                _state.value = VentaState.VentaExitosa
            } catch (e: Exception) {
                _state.value = current.copy()
                _state.value = VentaState.Error(e.message ?: "Error al confirmar la venta")
            }
        }
    }

    fun resetError() {
        val current = _state.value
        if (current is VentaState.Error) {
            cargarProductos()
        }
    }
}
