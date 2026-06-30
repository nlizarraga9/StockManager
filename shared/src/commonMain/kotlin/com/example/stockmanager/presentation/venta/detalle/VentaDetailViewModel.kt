package com.example.stockmanager.presentation.venta.detalle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockmanager.domain.model.Venta
import com.example.stockmanager.domain.repository.VentaRepository
import com.example.stockmanager.domain.usecase.venta.EliminarVentaUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class VentaDetailState {
    data object Loading : VentaDetailState()

    data class Loaded(
        val venta: Venta,
    ) : VentaDetailState()

    data object Deleting : VentaDetailState()

    data object Deleted : VentaDetailState()

    data class Error(
        val mensaje: String,
    ) : VentaDetailState()
}

class VentaDetailViewModel(
    private val repository: VentaRepository,
    private val eliminarVenta: EliminarVentaUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow<VentaDetailState>(VentaDetailState.Loading)
    val state: StateFlow<VentaDetailState> = _state.asStateFlow()

    fun cargarVenta(id: String) {
        viewModelScope.launch {
            _state.value = VentaDetailState.Loading
            try {
                val venta = repository.getVenta(id)
                _state.value = VentaDetailState.Loaded(venta)
            } catch (e: Exception) {
                _state.value = VentaDetailState.Error(e.message ?: "Error al cargar la venta")
            }
        }
    }

    fun eliminar(id: String) {
        viewModelScope.launch {
            _state.value = VentaDetailState.Deleting
            try {
                eliminarVenta(id)
                _state.value = VentaDetailState.Deleted
            } catch (e: Exception) {
                _state.value = VentaDetailState.Error(e.message ?: "Error al eliminar la venta")
            }
        }
    }

    fun resetError() {
        val current = _state.value
        if (current is VentaDetailState.Error) {
            _state.value = VentaDetailState.Loading
        }
    }
}
