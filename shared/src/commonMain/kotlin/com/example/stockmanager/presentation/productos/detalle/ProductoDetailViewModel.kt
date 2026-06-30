package com.example.stockmanager.presentation.productos.detalle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockmanager.domain.model.Producto
import com.example.stockmanager.domain.repository.ProductoRepository
import com.example.stockmanager.domain.usecase.producto.EliminarProductoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ProductoDetailState {
    data object Loading : ProductoDetailState()

    data class Loaded(
        val producto: Producto,
    ) : ProductoDetailState()

    data object Deleting : ProductoDetailState()

    data object Deleted : ProductoDetailState()

    data class Error(
        val mensaje: String,
    ) : ProductoDetailState()
}

class ProductoDetailViewModel(
    private val repository: ProductoRepository,
    private val eliminarProducto: EliminarProductoUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow<ProductoDetailState>(ProductoDetailState.Loading)
    val state: StateFlow<ProductoDetailState> = _state.asStateFlow()

    fun cargarProducto(id: String) {
        viewModelScope.launch {
            _state.value = ProductoDetailState.Loading
            try {
                val producto = repository.getProducto(id)
                _state.value = ProductoDetailState.Loaded(producto)
            } catch (e: Exception) {
                _state.value = ProductoDetailState.Error(e.message ?: "Error al eliminar el producto")
            }
        }
    }

    fun eliminar(id: String) {
        viewModelScope.launch {
            _state.value = ProductoDetailState.Deleting
            try {
                eliminarProducto(id)
                _state.value = ProductoDetailState.Deleted
            } catch (e: Exception) {
                _state.value = ProductoDetailState.Error(e.message ?: "Error al eliminar producto")
            }
        }
    }

    fun resetError() {
        val current = _state.value
        if (current is ProductoDetailState.Error) {
            _state.value = ProductoDetailState.Loading
        }
    }
}
