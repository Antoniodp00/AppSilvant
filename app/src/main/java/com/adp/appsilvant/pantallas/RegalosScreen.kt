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
import com.adp.appsilvant.data.Regalo
import io.github.jan.supabase.postgrest.postgrest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegalosScreen(navController: NavController) {

    var listaRegalos by remember { mutableStateOf<List<Regalo>>(emptyList()) }

    // Refreshes the list when returning from the detail screen
    LaunchedEffect(navController.currentBackStackEntry) {
        try {
            val regalos = SupabaseCliente.client.postgrest
                .from("regalos")
                .select()
                .decodeList<Regalo>()
            listaRegalos = regalos
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuestros Regalos") },
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
                navController.navigate("regalo_detail/-1") 
            }) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Regalo")
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
            items(listaRegalos) { regalo ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { 
                            // Navigate to detail screen with the specific gift's ID
                            navController.navigate("regalo_detail/${regalo.id}") 
                        },
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(regalo.nombreRegalo, style = MaterialTheme.typography.titleMedium)
                        regalo.fecha?.let { Text(it) }
                        regalo.descripcion?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                        regalo.tipo?.let { Text("Tipo: $it", style = MaterialTheme.typography.bodySmall) }
                    }
                }
            }
        }
    }
}
