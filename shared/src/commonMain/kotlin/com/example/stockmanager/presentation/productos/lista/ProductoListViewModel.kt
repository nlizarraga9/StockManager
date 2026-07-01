package com.example.stockmanager.presentation.productos.lista

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockmanager.domain.model.Producto
import com.example.stockmanager.domain.usecase.producto.GetProductosUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class ProductSortMode(val displayName: String) {
    ALPHABETICAL("Nombre (A-Z)"),
    STOCK_ASC("Menor stock"),
    STOCK_DESC("Mayor stock")
}

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

    private var allProducts: List<Producto> = emptyList()
    private val _sortMode = MutableStateFlow(ProductSortMode.ALPHABETICAL)
    val sortMode: StateFlow<ProductSortMode> = _sortMode.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun cargarProductos(forceSilent: Boolean = false) {
        viewModelScope.launch {
            if (!forceSilent || _state.value !is ProductoListState.Success) {
                _state.value = ProductoListState.Loading
            }
            try {
                allProducts = getProductos()
                actualizarEstado()
            } catch (e: Exception) {
                _state.value =
                    ProductoListState.Error(
                        e.message ?: "Error al cargar productos",
                    )
            }
        }
    }

    fun setSortMode(mode: ProductSortMode) {
        _sortMode.value = mode
        actualizarEstado()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        actualizarEstado()
    }

    private fun actualizarEstado() {
        val query = _searchQuery.value.trim()
        val filtered = if (query.isEmpty()) {
            allProducts
        } else {
            allProducts.filter {
                it.nombre.contains(query, ignoreCase = true) ||
                        (it.descripcion?.contains(query, ignoreCase = true) == true)
            }
        }

        if (filtered.isEmpty()) {
            if (allProducts.isEmpty()) {
                _state.value = ProductoListState.Empty
            } else {
                _state.value = ProductoListState.Success(emptyList())
            }
            return
        }

        val sorted = when (_sortMode.value) {
            ProductSortMode.ALPHABETICAL -> {
                filtered.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.nombre })
            }
            ProductSortMode.STOCK_ASC -> {
                filtered.sortedWith(compareBy<Producto> { it.stock }.thenBy(String.CASE_INSENSITIVE_ORDER) { it.nombre })
            }
            ProductSortMode.STOCK_DESC -> {
                filtered.sortedWith(compareByDescending<Producto> { it.stock }.thenBy(String.CASE_INSENSITIVE_ORDER) { it.nombre })
            }
        }
        _state.value = ProductoListState.Success(sorted)
    }
}
