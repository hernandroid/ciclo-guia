package com.cicloguia.app.core.network

interface HttpClient {
    suspend fun get(url: String): String
}