package com.adp.appsilvant

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.URLProtocol
import io.ktor.http.path // *** THE MISSING IMPORT ***
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object TMDbCliente {

    // *** IMPORTANT: Replace with your actual TMDb API Key ***
    private const val TMDB_API_KEY = "377436a43ac5d7e3db5d9d058102d17b"

    val client = HttpClient(Android) {

        install(ContentNegotiation) {
            json(Json { 
                ignoreUnknownKeys = true
                prettyPrint = true
            })
        }

        install(Logging) {
            level = LogLevel.ALL
        }

        // Configure default request parameters for all outgoing requests
        defaultRequest {
            url {
                protocol = URLProtocol.HTTPS
                host = "api.themoviedb.org"
                path("3/")
                parameters.append("api_key", TMDB_API_KEY)
                parameters.append("language", "es-ES")
            }
        }
    }
}