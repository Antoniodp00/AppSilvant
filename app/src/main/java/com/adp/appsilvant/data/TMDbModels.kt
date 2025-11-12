package com.adp.appsilvant.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Represents the overall search result from TMDb (generic paged response)
@Serializable
data class PagedResponse<T>(
    val results: List<T> = emptyList()
)

// Represents a single item (movie or TV show) in the search results
@Serializable
data class TMDbMediaItem(
    val id: Int,
    @SerialName("media_type")
    val mediaType: String, // "movie" or "tv"
    // Use @SerialName to map JSON keys to different field names
    @SerialName("name")
    val name: String? = null, // For TV shows
    @SerialName("title")
    val title: String? = null, // For movies
    @SerialName("poster_path")
    val posterPath: String? = null
) {
    // Helper property to get the display title regardless of media type
    val displayTitle: String
        get() = title ?: name ?: "Título no disponible"
}

// Minimal models for TMDb lists
@Serializable
data class MovieResult(
    val id: Int,
    val title: String? = null,
    @SerialName("poster_path") val posterPath: String? = null
)

@Serializable
data class TvResult(
    val id: Int,
    val name: String? = null,
    @SerialName("poster_path") val posterPath: String? = null
)
