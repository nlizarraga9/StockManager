package com.example.stockmanager.di

import com.example.stockmanager.data.repository.ProductoRepositoryImpl
import com.example.stockmanager.data.repository.VentaRepositoryImpl
import com.example.stockmanager.domain.repository.ProductoRepository
import com.example.stockmanager.domain.repository.VentaRepository
import com.example.stockmanager.domain.usecase.producto.EliminarProductoUseCase
import com.example.stockmanager.domain.usecase.producto.GetProductosUseCase
import com.example.stockmanager.domain.usecase.venta.CrearVentaUseCase
import com.example.stockmanager.domain.usecase.venta.EliminarVentaUseCase
import com.example.stockmanager.presentation.productos.detalle.ProductoDetailViewModel
import com.example.stockmanager.presentation.productos.form.ProductoFormViewModel
import com.example.stockmanager.presentation.productos.lista.ProductoListViewModel
import com.example.stockmanager.presentation.venta.detalle.VentaDetailViewModel
import com.example.stockmanager.presentation.venta.lista.VentaListViewModel
import com.example.stockmanager.presentation.venta.nueva.VentaViewModel
import org.koin.dsl.module

val appModule =
    module {
        // Repositories
        single<ProductoRepository> { ProductoRepositoryImpl() }
        single<VentaRepository> { VentaRepositoryImpl() }

        // UseCases
        factory { GetProductosUseCase(get()) }
        factory { CrearVentaUseCase(ventaRepository = get(), productoRepository = get()) }
        factory { EliminarProductoUseCase(get()) }
        factory { EliminarVentaUseCase(ventaRepository = get(), productoRepository = get()) }

        // ViewModels
        factory { ProductoListViewModel(getProductos = get(), repository = get()) }
        factory { ProductoFormViewModel(get()) }
        factory { ProductoDetailViewModel(repository = get(), eliminarProducto = get()) }
        factory { VentaViewModel(getProductos = get(), crearVenta = get()) }
        factory { VentaListViewModel(repository = get(), crearVenta = get()) }
        factory { VentaDetailViewModel(repository = get(), eliminarVenta = get()) }
    }
