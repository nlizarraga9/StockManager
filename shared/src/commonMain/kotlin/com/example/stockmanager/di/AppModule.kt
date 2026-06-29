package com.example.stockmanager.di

import com.example.stockmanager.data.repository.ProductoRepositoryImpl
import com.example.stockmanager.domain.repository.ProductoRepository
import org.koin.dsl.module

val appModule =
    module {
        single<ProductoRepository> { ProductoRepositoryImpl() }
    }
