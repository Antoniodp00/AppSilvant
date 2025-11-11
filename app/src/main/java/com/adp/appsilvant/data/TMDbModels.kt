package com.adp.appsilvant.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Represents the overall search result from TMDb
@Serializable
data class TMDbSearchResponse(
    val results: List<TMDbMediaItem>
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
