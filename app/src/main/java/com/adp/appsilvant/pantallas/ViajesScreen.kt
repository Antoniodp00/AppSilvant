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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.adp.appsilvant.SupabaseCliente
import com.adp.appsilvant.data.Viaje
import io.github.jan.supabase.postgrest.postgrest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViajesScreen(navController: NavController) {

    var listaViajes by remember { mutableStateOf<List<Viaje>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(navController.currentBackStackEntry) {
        isLoading = true
        try {
            val viajes = SupabaseCliente.client.postgrest
                .from("viajes")
                .select()
                .decodeList<Viaje>()
            listaViajes = viajes
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuestros Viajes") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver atrás")
                    }
                }
            )
        },
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
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(viaje.lugar, style = MaterialTheme.typography.titleMedium)
                                    viaje.fecha?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                                    viaje.descripcion?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
