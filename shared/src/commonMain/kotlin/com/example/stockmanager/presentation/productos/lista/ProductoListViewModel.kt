package com.example.stockmanager.presentation.productos.lista

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockmanager.domain.model.Producto
import com.example.stockmanager.domain.usecase.GetProductosUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ProductoListState {
    data object Loading : ProductoListState()

    data class Success(
        val productos: List<Producto>,
    ) : ProductoListState()

    data class Error(
        val mensaje: String,
    ) : ProductoListState()

    data object Empty : ProductoListState()
}

class ProductoListViewModel(
    private val getProductos: GetProductosUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow<ProductoListState>(ProductoListState.Loading)
    val state: StateFlow<ProductoListState> = _state.asStateFlow()

    fun cargarProductos(forceSilent: Boolean = false) {
        viewModelScope.launch {
            if (!forceSilent || _state.value !is ProductoListState.Success) {
                _state.value = ProductoListState.Loading
            }
            try {
                val productos = getProductos()
                _state.value =
                    if (productos.isEmpty()) {
                        ProductoListState.Empty
                    } else {
                        ProductoListState.Success(productos)
                    }
            } catch (e: Exception) {
                _state.value =
                    ProductoListState.Error(
                        e.message ?: "Error al cargar productos",
                    )
            }
        }
    }
}
