package com.adp.appsilvant.pantallas

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.adp.appsilvant.SupabaseCliente
import com.adp.appsilvant.data.Viaje
import io.github.jan.supabase.postgrest.postgrest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViajesScreen(navController: NavController) {

    var listaViajes by remember { mutableStateOf<List<Viaje>>(emptyList()) }

    // Refreshes the list when returning from the detail screen
    LaunchedEffect(navController.currentBackStackEntry) {
        try {
            val viajes = SupabaseCliente.client.postgrest
                .from("viajes")
                .select()
                .decodeList<Viaje>()
            listaViajes = viajes
        } catch (e: Exception) {
            e.printStackTrace()
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
            FloatingActionButton(onClick = { 
                // Navigate to detail screen with -1 to signify "create mode"
                navController.navigate("viaje_detail/-1") 
            }) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Viaje")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(listaViajes) { viaje ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { 
                            // Navigate to detail screen with the specific trip's ID
                            navController.navigate("viaje_detail/${viaje.id}") 
                        },
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(viaje.lugar, style = MaterialTheme.typography.titleMedium)
                        viaje.fecha?.let { Text(it) }
                        viaje.descripcion?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                    }
                }
            }
        }
    }
}
