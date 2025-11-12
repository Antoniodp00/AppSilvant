package com.adp.appsilvant.pantallas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.adp.appsilvant.data.PagedResponse
import com.adp.appsilvant.data.MovieResult
import com.adp.appsilvant.data.TvResult
import io.github.jan.supabase.postgrest.postgrest
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.path
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaScreen(navController: NavController) {

    // Estados base
    var savedMedia by remember { mutableStateOf<List<MediaVisto>>(emptyList()) }
    var isLoadingSaved by remember { mutableStateOf(true) }

    // Estados para pestañas de TMDb
    var selectedTabIndex by remember { mutableStateOf(0) } // 0=Populares, 1=Más Valoradas, 2=Mis Vistas

    var populares by remember { mutableStateOf<List<TMDbMediaItem>>(emptyList()) }
    var topRated by remember { mutableStateOf<List<TMDbMediaItem>>(emptyList()) }
    var tvPopular by remember { mutableStateOf<List<TMDbMediaItem>>(emptyList()) } // cache para "Serie Aleatoria"

    var isLoadingPopular by remember { mutableStateOf(false) }
    var isLoadingTopRated by remember { mutableStateOf(false) }

    var errorPopular by remember { mutableStateOf<String?>(null) }
    var errorTopRated by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    // Modelos de TMDb se definen a nivel superior en com.adp.appsilvant.data.TMDbModels.kt

    fun MovieResult.toMediaItem() = TMDbMediaItem(
        id = id,
        mediaType = "movie",
        name = null,
        title = title,
        posterPath = posterPath
    )

    fun TvResult.toMediaItem() = TMDbMediaItem(
        id = id,
        mediaType = "tv",
        name = name,
        title = null,
        posterPath = posterPath
    )

    fun fetchSavedMedia() {
        scope.launch {
            isLoadingSaved = true
            try {
                val result = SupabaseCliente.client.postgrest
                    .from("media_vistos")
                    .select()
                    .decodeList<MediaVisto>()
                savedMedia = result
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoadingSaved = false
            }
        }
    }

    suspend fun loadPopularIfNeeded(force: Boolean = false) {
        if (populares.isNotEmpty() && !force) return
        isLoadingPopular = true
        errorPopular = null
        try {
            val response: PagedResponse<MovieResult> = TMDbCliente.client.get("movie/popular").body()
            populares = response.results.map { it.toMediaItem() }
        } catch (e: Exception) {
            errorPopular = e.message
        } finally {
            isLoadingPopular = false
        }
    }

    suspend fun loadTopRatedIfNeeded(force: Boolean = false) {
        if (topRated.isNotEmpty() && !force) return
        isLoadingTopRated = true
        errorTopRated = null
        try {
            val response: PagedResponse<MovieResult> = TMDbCliente.client.get("movie/top_rated").body()
            topRated = response.results.map { it.toMediaItem() }
        } catch (e: Exception) {
            errorTopRated = e.message
        } finally {
            isLoadingTopRated = false
        }
    }

    suspend fun loadTvPopularIfNeeded(force: Boolean = false) {
        if (tvPopular.isNotEmpty() && !force) return
        try {
            val response: PagedResponse<TvResult> = TMDbCliente.client.get("tv/popular").body()
            tvPopular = response.results.map { it.toMediaItem() }
        } catch (_: Exception) {
            // silencioso: botón aleatorio manejará fallback
        }
    }

    // Cargar "Mis Vistas" al entrar a la pantalla
    LaunchedEffect(navController.currentBackStackEntry) {
        fetchSavedMedia()
    }

    // Cargar datos al cambiar de pestaña cuando sea necesario
    LaunchedEffect(selectedTabIndex) {
        when (selectedTabIndex) {
            0 -> loadPopularIfNeeded()
            1 -> loadTopRatedIfNeeded()
            2 -> { /* Mis Vistas: ya se carga con Supabase */ }
        }
    }

    // Contenido principal
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(12.dp))
        // --- Botones Aleatorios y acción de búsqueda opcional ---
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                scope.launch {
                    // Asegura popular cargado
                    loadPopularIfNeeded()
                    val item = populares.randomOrNull()
                    if (item != null) {
                        try {
                            val nuevo = com.adp.appsilvant.data.MediaVisto(
                                mediaId = item.id,
                                titulo = item.displayTitle,
                                tipo = "movie",
                                posterPath = item.posterPath
                            )
                            val saved = com.adp.appsilvant.SupabaseCliente.client.postgrest
                                .from("media_vistos")
                                .insert(nuevo)
                                .decodeSingle<com.adp.appsilvant.data.MediaVisto>()
                            navController.navigate("media_detail/${'$'}{saved.id}")
                        } catch (e: Exception) {
                            // fallback: navegar con un id inválido no tiene sentido; podríamos mostrar snackbar si tuvieramos host
                        }
                    }
                }
            }) { Text("Película Aleatoria") }

            Button(onClick = {
                scope.launch {
                    loadTvPopularIfNeeded()
                    val item = tvPopular.randomOrNull()
                    if (item != null) {
                        try {
                            val nuevo = com.adp.appsilvant.data.MediaVisto(
                                mediaId = item.id,
                                titulo = item.displayTitle,
                                tipo = "tv",
                                posterPath = item.posterPath
                            )
                            val saved = com.adp.appsilvant.SupabaseCliente.client.postgrest
                                .from("media_vistos")
                                .insert(nuevo)
                                .decodeSingle<com.adp.appsilvant.data.MediaVisto>()
                            navController.navigate("media_detail/${'$'}{saved.id}")
                        } catch (e: Exception) {
                            // handle silently for now
                        }
                    }
                }
            }) { Text("Serie Aleatoria") }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- Pestañas ---
        val tabs = listOf("Populares", "Más Valoradas", "Mis Vistas")
        TabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }

        // --- Contenido de pestañas ---
        when (selectedTabIndex) {
            0 -> {
                when {
                    isLoadingPopular -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    errorPopular != null -> Text(
                        text = "Error al cargar Populares: ${'$'}errorPopular",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                    else -> MediaList(items = populares) { item ->
                        navController.navigate("media_detail/${item.id.toLong()}")
                    }
                }
            }
            1 -> {
                when {
                    isLoadingTopRated -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    errorTopRated != null -> Text(
                        text = "Error al cargar Más Valoradas: ${'$'}errorTopRated",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                    else -> MediaList(items = topRated) { item ->
                        navController.navigate("media_detail/${item.id.toLong()}")
                    }
                }
            }
            2 -> SavedMediaList(navController, savedMedia, isLoadingSaved)
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
                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { navController.navigate("media_detail/${item.id}") }
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
    OutlinedCard(modifier = modifier.fillMaxWidth()) {
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


@Composable
private fun MediaList(items: List<TMDbMediaItem>, onItemClick: (TMDbMediaItem) -> Unit) {
    if (items.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "Sin elementos por ahora.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(32.dp)
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .animateContentSize(),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items, key = { it.id }) { item ->
                AnimatedVisibility(visible = true, enter = fadeIn()) {
                    MediaListItem(item, Modifier.clickable { onItemClick(item) })
                }
            }
        }
    }
}
