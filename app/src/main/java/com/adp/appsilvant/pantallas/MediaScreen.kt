package com.adp.appsilvant.pantallas

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.adp.appsilvant.SupabaseCliente
import com.adp.appsilvant.TMDbCliente
import com.adp.appsilvant.data.MediaVisto
import com.adp.appsilvant.data.TMDbMediaItem
import com.adp.appsilvant.data.TMDbSearchResponse
import io.github.jan.supabase.postgrest.postgrest
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaScreen(navController: NavController) {

    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<TMDbMediaItem>>(emptyList()) }
    var savedMedia by remember { mutableStateOf<List<MediaVisto>>(emptyList()) }
    val scope = rememberCoroutineScope()

    fun fetchSavedMedia() {
        scope.launch {
            try {
                val result = SupabaseCliente.client.postgrest
                    .from("media_vistos")
                    .select()
                    .decodeList<MediaVisto>()
                savedMedia = result
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Use LaunchedEffect with a changing key to refresh when navigating back
    LaunchedEffect(navController.currentBackStackEntry) {
        fetchSavedMedia()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Series y Películas") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver atrás"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {

            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Buscar película o serie") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    scope.launch {
                        try {
                            val response: TMDbSearchResponse = TMDbCliente.client.get("search/multi") {
                                parameter("query", searchQuery)
                            }.body()
                            searchResults = response.results.filter { it.mediaType == "movie" || it.mediaType == "tv" }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }) {
                    Text("Buscar")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            var selectedTabIndex by remember { mutableStateOf(0) }
            val tabs = listOf("Resultados de Búsqueda", "Mis Vistas")

            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTabIndex) {
                0 -> SearchResultsList(searchResults) { item ->
                    val newMedia = MediaVisto(
                        mediaId = item.id,
                        titulo = item.displayTitle,
                        tipo = item.mediaType,
                        posterPath = item.posterPath
                    )
                    scope.launch {
                        try {
                            SupabaseCliente.client.postgrest.from("media_vistos").insert(newMedia)
                            fetchSavedMedia()
                            selectedTabIndex = 1
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                1 -> SavedMediaList(savedMedia) { media ->
                    navController.navigate("media_detail/${media.id}")
                }
            }
        }
    }
}

@Composable
private fun SearchResultsList(results: List<TMDbMediaItem>, onItemClick: (TMDbMediaItem) -> Unit) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(results) { item ->
            MediaListItem(item, Modifier.clickable { onItemClick(item) })
        }
    }
}

@Composable
private fun SavedMediaList(mediaList: List<MediaVisto>, onItemClick: (MediaVisto) -> Unit) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(mediaList) { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onItemClick(item) } // Make the whole card clickable
            ) {
                Row(modifier = Modifier.padding(8.dp)) {
                    AsyncImage(
                        model = "https://image.tmdb.org/t/p/w500${item.posterPath}",
                        contentDescription = item.titulo,
                        modifier = Modifier.height(120.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(item.titulo, style = MaterialTheme.typography.titleMedium)
                        Text("Estado: ${item.estado}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun MediaListItem(item: TMDbMediaItem, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(8.dp)) {
            AsyncImage(
                model = "https://image.tmdb.org/t/p/w500${item.posterPath}",
                contentDescription = item.displayTitle,
                modifier = Modifier.height(120.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(item.displayTitle, style = MaterialTheme.typography.titleMedium)
        }
    }
}
