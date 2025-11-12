package com.adp.appsilvant.pantallas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.adp.appsilvant.SupabaseCliente
import com.adp.appsilvant.data.Viaje
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.launch
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.compose.currentBackStackEntryAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViajesScreen(navController: NavController) {

    var listaViajes by remember { mutableStateOf<List<Viaje>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val shouldRefresh = currentBackStackEntry?.savedStateHandle?.get<Boolean>("shouldRefresh") ?: false
    fun loadData() {
        isLoading = true
        try {
            // Launch in composition-safe way
            // But since this is quick, use LaunchedEffect scope below
        } catch (_: Exception) {}
    }

    // Carga inicial
    LaunchedEffect(currentBackStackEntry, shouldRefresh) {
        // 3. Si 'shouldRefresh' es true, o si la lista está vacía, carga los datos
        if (shouldRefresh || listaViajes.isEmpty()) {
            isLoading = true
            try {
                val viajes = SupabaseCliente.client.postgrest
                    .from("viajes")
                    .select()
                    .decodeList<Viaje>()
                listaViajes = viajes
                // 4. Resetea el flag para que no recargue en bucle
                currentBackStackEntry?.savedStateHandle?.set("shouldRefresh", false)
            } catch (e: Exception) {
                e.printStackTrace()
                listaViajes = emptyList()
            }
            isLoading = false
        }
    }

    // Recargar al volver a la pantalla
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                // Recargar datos
                val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main)
                scope.launch {
                    isLoading = true
                    try {
                        val viajes = SupabaseCliente.client.postgrest
                            .from("viajes")
                            .select(columns = Columns.raw("*, fotos_viajes(url_foto,limit=1)"))
                            .decodeList<Viaje>()
                        listaViajes = viajes
                    } catch (_: Exception) {
                    } finally {
                        isLoading = false
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("viaje_detail/-1") }) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Viaje")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else if (listaViajes.isEmpty()) {
                Text(
                    text = "Aún no hay viajes. ¡Pulsa el botón '+' para añadir el primero!",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(32.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .animateContentSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(listaViajes, key = { it.id }) { viaje ->
                        AnimatedVisibility(visible = true, enter = fadeIn()) { 
                            OutlinedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { 
                                        navController.navigate("viaje_detail/${viaje.id}") 
                                    }
                            ) {
                                val portada = viaje.fotos.firstOrNull()?.urlFoto
                                AsyncImage(
                                    model = portada,
                                    contentDescription = "Foto de portada del viaje",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp),
                                    contentScale = ContentScale.Crop
                                )
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(viaje.lugar, style = MaterialTheme.typography.titleMedium)
                                    viaje.fecha?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
