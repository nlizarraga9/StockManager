package com.example.stockmanager

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform