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
import com.adp.appsilvant.data.Regalo
import io.github.jan.supabase.postgrest.postgrest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegalosScreen(navController: NavController) {

    var listaRegalos by remember { mutableStateOf<List<Regalo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(navController.currentBackStackEntry) {
        isLoading = true
        try {
            val regalos = SupabaseCliente.client.postgrest
                .from("regalos")
                .select()
                .decodeList<Regalo>()
            listaRegalos = regalos
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
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
            FloatingActionButton(onClick = { navController.navigate("regalo_detail/-1") }) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Regalo")
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
            } else if (listaRegalos.isEmpty()) {
                Text(
                    text = "Aún no hay regalos. ¡Pulsa el botón '+' para añadir el primero!",
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
                    items(listaRegalos, key = { it.id }) { regalo ->
                        AnimatedVisibility(visible = true, enter = fadeIn()) { 
                            OutlinedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { 
                                        navController.navigate("regalo_detail/${regalo.id}") 
                                    }
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(regalo.nombreRegalo, style = MaterialTheme.typography.titleMedium)
                                    regalo.fecha?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                                    regalo.descripcion?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                                    regalo.tipo?.let { Text("Tipo: $it", style = MaterialTheme.typography.bodySmall) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
