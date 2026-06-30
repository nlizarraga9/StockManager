package com.example.stockmanager.di

import com.example.stockmanager.data.repository.ProductoRepositoryImpl
import com.example.stockmanager.data.repository.VentaRepositoryImpl
import com.example.stockmanager.domain.repository.ProductoRepository
import com.example.stockmanager.domain.repository.VentaRepository
import com.example.stockmanager.domain.usecase.producto.GetProductosUseCase
import com.example.stockmanager.domain.usecase.venta.CrearVentaUseCase
import com.example.stockmanager.presentation.productos.form.ProductoFormViewModel
import com.example.stockmanager.presentation.productos.lista.ProductoListViewModel
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

        // ViewModels
        factory { ProductoListViewModel(get()) }
        factory { ProductoFormViewModel(get()) }
        factory { VentaViewModel(getProductos = get(), crearVenta = get()) }
    }
