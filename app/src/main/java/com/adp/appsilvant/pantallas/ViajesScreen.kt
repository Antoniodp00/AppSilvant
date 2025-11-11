package com.adp.appsilvant.pantallas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.adp.appsilvant.SupabaseCliente
import com.adp.appsilvant.data.Viaje
import com.adp.appsilvant.ui.theme.AppSilvantTheme
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViajesScreen(navController: NavController) {

    var lugar by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var listaViajes by remember { mutableStateOf<List<Viaje>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(true) {
        try {
            val viajes = SupabaseCliente.client.postgrest
                .from("viajes")
                .select()
                .decodeList<Viaje>()
            listaViajes = viajes
        } catch (e: Exception) {
            println("Error detallado al cargar viajes:")
            e.printStackTrace()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuestros Viajes") },
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
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp) // Original padding
        ) {
            Text("Añadir Nuevo Viaje", style = MaterialTheme.typography.titleLarge)

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = lugar,
                onValueChange = { lugar = it },
                label = { Text("Lugar (Obligatorio)") },
                modifier = Modifier.fillMaxWidth(),
                isError = lugar.isBlank()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = fecha,
                onValueChange = { fecha = it },
                label = { Text("Fecha (AAAA-MM-DD)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val nuevoViaje = Viaje(
                        lugar = lugar,
                        fecha = fecha.ifEmpty { null },
                        descripcion = descripcion.ifEmpty { null }
                    )

                    scope.launch {
                        try {
                            SupabaseCliente.client.postgrest
                                .from("viajes")
                                .insert(nuevoViaje)

                            lugar = ""
                            fecha = ""
                            descripcion = ""

                            val viajesActualizados = SupabaseCliente.client.postgrest
                                .from("viajes")
                                .select()
                                .decodeList<Viaje>()
                            listaViajes = viajesActualizados

                        } catch (e: Exception) {
                            println("Error detallado al guardar viaje:")
                            e.printStackTrace()
                        }
                    }
                },
                enabled = lugar.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar Viaje")
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text("Viajes Guardados", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(listaViajes) { viaje ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
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
}

@Preview(showBackground = true)
@Composable
fun PreviewViajesScreen() {
    AppSilvantTheme {
        ViajesScreen(navController = rememberNavController())
    }
}
