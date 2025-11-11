package com.adp.appsilvant.pantallas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
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
    var isLoading by remember { mutableStateOf(true) } // State for loading

    val scope = rememberCoroutineScope()

    // Function to fetch saved media from Supabase
    fun fetchSavedMedia() {
        scope.launch {
            isLoading = true
            try {
                val result = SupabaseCliente.client.postgrest
                    .from("media_vistos")
                    .select()
                    .decodeList<MediaVisto>()
                savedMedia = result
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false // Ensure loading is always turned off
            }
        }
    }

    // Load saved media when the screen first appears or when returning to it
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
        Column(modifier = Modifier.padding(padding).padding(horizontal = 16.dp)) {

            // --- SEARCH UI ---
            Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
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

            // --- TABS to switch between Search and Saved ---
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

            // --- CONTENT AREA ---
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
                            fetchSavedMedia() // Refresh the saved list
                            selectedTabIndex = 1 // Switch to the saved list tab
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                1 -> SavedMediaList(navController, savedMedia, isLoading)
            }
        }
    }
}

@Composable
private fun SearchResultsList(results: List<TMDbMediaItem>, onItemClick: (TMDbMediaItem) -> Unit) {
    if (results.isEmpty()) {
         Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
             Text(
                text = "Busca una película o serie para empezar.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(32.dp)
            )
         }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().animateContentSize(), 
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(results, key = { it.id }) { item ->
                AnimatedVisibility(visible = true, enter = fadeIn()) {
                    MediaListItem(item, Modifier.clickable { onItemClick(item) })
                }
            }
        }
    }
}

@Composable
private fun SavedMediaList(navController: NavController, mediaList: List<MediaVisto>, isLoading: Boolean) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (isLoading) {
            CircularProgressIndicator()
        } else if (mediaList.isEmpty()) {
            Text(
                text = "Aún no has guardado ninguna película o serie.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(32.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().animateContentSize(),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(mediaList, key = { it.id }) { item ->
                     AnimatedVisibility(visible = true, enter = fadeIn()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { navController.navigate("media_detail/${item.id}") },
                            colors = CardDefaults.outlinedCardColors()
                        ) {
                            Row(modifier = Modifier.padding(16.dp)) {
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
        }
    }
}

@Composable
private fun MediaListItem(item: TMDbMediaItem, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth(), colors = CardDefaults.outlinedCardColors()) {
        Row(modifier = Modifier.padding(16.dp)) {
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
