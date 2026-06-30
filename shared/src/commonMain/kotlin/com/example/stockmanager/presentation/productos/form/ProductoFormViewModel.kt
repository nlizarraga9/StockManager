package com.example.stockmanager.presentation.productos.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockmanager.domain.model.Producto
import com.example.stockmanager.domain.repository.ProductoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ProductoFormState {
    data object Idle : ProductoFormState()

    data object Loading : ProductoFormState()

    data class Loaded(
        val producto: Producto,
    ) : ProductoFormState()

    data object Saving : ProductoFormState()

    data object SaveSuccess : ProductoFormState()

    data class Error(
        val mensaje: String,
    ) : ProductoFormState()
}

class ProductoFormViewModel(
    private val repository: ProductoRepository,
) : ViewModel() {
    private val _state = MutableStateFlow<ProductoFormState>(ProductoFormState.Idle)
    val state: StateFlow<ProductoFormState> = _state.asStateFlow()

    fun cargarProducto(id: String) {
        viewModelScope.launch {
            _state.value = ProductoFormState.Loading
            try {
                val producto = repository.getProducto(id)
                _state.value = ProductoFormState.Loaded(producto)
            } catch (e: Exception) {
                _state.value = ProductoFormState.Error(e.message ?: "Error al cargar producto")
            }
        }
    }

    fun guardarProducto(
        id: String?,
        nombre: String,
        descripcion: String,
        precio: String,
        stock: String,
        stockMinimo: String,
    ) {
        val precioDouble = precio.replace(",", ".").toDoubleOrNull()
        val stockInt = stock.toIntOrNull()
        val stockMinimoInt = stockMinimo.toIntOrNull() ?: 5

        if (nombre.isBlank()) {
            _state.value = ProductoFormState.Error("El nombre es obligatorio")
            return
        }
        if (precioDouble == null || precioDouble < 0) {
            _state.value = ProductoFormState.Error("El precio debe ser un número válido")
            return
        }
        if (stockInt == null || stockInt < 0) {
            _state.value = ProductoFormState.Error("El stock debe ser un número válido")
            return
        }

        viewModelScope.launch {
            _state.value = ProductoFormState.Saving
            try {
                val producto =
                    Producto(
                        id = id ?: "",
                        nombre = nombre.trim(),
                        descripcion = descripcion.trim().ifBlank { null },
                        precio = precioDouble,
                        stock = stockInt,
                        stockMinimo = stockMinimoInt,
                    )
                if (id == null) {
                    repository.insertProducto(producto)
                } else {
                    repository.updateProducto(producto)
                }
                _state.value = ProductoFormState.SaveSuccess
            } catch (e: Exception) {
                _state.value = ProductoFormState.Error(e.message ?: "Error al guardar")
            }
        }
    }

    fun resetError() {
        _state.value = ProductoFormState.Idle
    }
}
