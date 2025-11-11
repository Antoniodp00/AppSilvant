package com.adp.appsilvant.pantallas

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.adp.appsilvant.SupabaseCliente
import com.adp.appsilvant.data.MediaVisto
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaDetailScreen(navController: NavController, mediaId: Long) {

    var mediaItem by remember { mutableStateOf<MediaVisto?>(null) }
    var selectedStatus by remember { mutableStateOf("viendo") }
    var selectedRating by remember { mutableStateOf(0f) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(mediaId) {
        try {
            val result = SupabaseCliente.client.postgrest
                .from("media_vistos")
                .select { filter { eq("id", mediaId) } }
                .decodeSingleOrNull<MediaVisto>()
            
            mediaItem = result
            result?.let {
                selectedStatus = it.estado
                selectedRating = it.valoracion?.toFloat() ?: 0f
            }
        } catch (e: Exception) {
            scope.launch { snackbarHostState.showSnackbar("Error al cargar datos: ${e.message}") }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("¿Confirmar borrado?") },
            text = { Text("Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                SupabaseCliente.client.postgrest.from("media_vistos").delete { 
                                    filter { eq("id", mediaId) }
                                }
                                showDeleteDialog = false
                                navController.popBackStack()
                            } catch (e: Exception) {
                                scope.launch { snackbarHostState.showSnackbar("Error al borrar: ${e.message}") }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(mediaItem?.titulo ?: "Cargando...") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        mediaItem?.let { item ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    model = "https://image.tmdb.org/t/p/w500${item.posterPath}",
                    contentDescription = item.titulo,
                    modifier = Modifier.height(300.dp).fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(item.titulo, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                
                Spacer(modifier = Modifier.height(24.dp))

                val statusOptions = listOf("Viendo", "Terminada")
                SingleChoiceSegmentedButtonRow {
                    statusOptions.forEachIndexed { index, label ->
                        SegmentedButton(
                            selected = selectedStatus.equals(label, ignoreCase = true),
                            onClick = { selectedStatus = label.lowercase() },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = statusOptions.size)
                        ) {
                            Text(label)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text("Valoración: ${selectedRating.roundToInt()}/5", style = MaterialTheme.typography.bodyLarge)
                Slider(
                    value = selectedRating,
                    onValueChange = { selectedRating = it },
                    valueRange = 0f..5f,
                    steps = 4
                )

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        scope.launch {
                            try {
                                val updates = mapOf("estado" to selectedStatus, "valoracion" to selectedRating.roundToInt())
                                SupabaseCliente.client.postgrest.from("media_vistos").update(updates) { 
                                    filter { eq("id", mediaId) }
                                }
                                navController.popBackStack()
                            } catch (e: Exception) {
                                scope.launch { snackbarHostState.showSnackbar("Error al actualizar: ${e.message}") }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Guardar Cambios")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { showDeleteDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Borrar Item")
                }
            }
        }
    }
}
