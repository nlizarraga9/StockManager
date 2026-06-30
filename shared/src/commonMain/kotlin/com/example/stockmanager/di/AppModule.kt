package com.example.stockmanager.di

import com.example.stockmanager.data.repository.ProductoRepositoryImpl
import com.example.stockmanager.domain.repository.ProductoRepository
import com.example.stockmanager.domain.usecase.GetProductosUseCase
import com.example.stockmanager.presentation.productos.lista.ProductoListViewModel
import org.koin.dsl.module

val appModule =
    module {
        single<ProductoRepository> { ProductoRepositoryImpl() }
        factory { GetProductosUseCase(get()) }
        factory { ProductoListViewModel(get()) }
    }
