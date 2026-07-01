package com.example.stockmanager.presentation.venta.lista

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stockmanager.domain.model.Venta
import com.example.stockmanager.domain.model.VentaItem
import com.example.stockmanager.domain.repository.VentaRepository
import com.example.stockmanager.domain.usecase.venta.CrearVentaUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class VentaListState {
    data object Loading : VentaListState()

    data class Success(
        val ventas: List<Venta>,
    ) : VentaListState()

    data class Error(
        val mensaje: String,
    ) : VentaListState()

    data object Empty : VentaListState()
}

class VentaListViewModel(
    private val repository: VentaRepository,
    private val crearVenta: CrearVentaUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow<VentaListState>(VentaListState.Loading)
    val state: StateFlow<VentaListState> = _state.asStateFlow()

    init {
        cargarVentas()
    }

    fun cargarVentas() {
        viewModelScope.launch {
            _state.value = VentaListState.Loading
            try {
                val ventas = repository.getVentas().sortedByDescending { it.createdAt }
                _state.value =
                    if (ventas.isEmpty()) {
                        VentaListState.Empty
                    } else {
                        VentaListState.Success(ventas)
                    }
            } catch (e: Exception) {
                _state.value = VentaListState.Error(e.message ?: "Error al cargar ventas")
            }
        }
    }

    fun deshacerEliminar(items: List<VentaItem>) {
        viewModelScope.launch {
            try {
                crearVenta(items)
                cargarVentas()
            } catch (e: Exception) {
                _state.value = VentaListState.Error(e.message ?: "Error al restaurar la venta")
            }
        }
    }
}
