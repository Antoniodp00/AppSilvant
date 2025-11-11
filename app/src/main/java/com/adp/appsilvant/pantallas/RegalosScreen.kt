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
import androidx.navigation.NavController
import com.adp.appsilvant.SupabaseCliente
import com.adp.appsilvant.data.Regalo
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegalosScreen(navController: NavController) {

    var nombreRegalo by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("") }
    var listaRegalos by remember { mutableStateOf<List<Regalo>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(true) {
        try {
            val regalos = SupabaseCliente.client.postgrest
                .from("regalos")
                .select()
                .decodeList<Regalo>()
            listaRegalos = regalos
        } catch (e: Exception) {
            println("Error al cargar regalos: ${e.message}")
            e.printStackTrace()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuestros Regalos") },
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
                .padding(16.dp)
        ) {
            Text("Añadir Nuevo Regalo", style = MaterialTheme.typography.titleLarge)

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = nombreRegalo,
                onValueChange = { nombreRegalo = it },
                label = { Text("Nombre del Regalo (Obligatorio)") },
                modifier = Modifier.fillMaxWidth(),
                isError = nombreRegalo.isBlank()
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
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = tipo,
                onValueChange = { tipo = it },
                label = { Text("Tipo (ej. regalado/recibido)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val nuevoRegalo = Regalo(
                        nombreRegalo = nombreRegalo,
                        fecha = fecha.ifEmpty { null },
                        descripcion = descripcion.ifEmpty { null },
                        tipo = tipo.ifEmpty { null }
                    )

                    scope.launch {
                        try {
                            SupabaseCliente.client.postgrest
                                .from("regalos")
                                .insert(nuevoRegalo)

                            nombreRegalo = ""
                            fecha = ""
                            descripcion = ""
                            tipo = ""

                            val regalosActualizados = SupabaseCliente.client.postgrest
                                .from("regalos")
                                .select()
                                .decodeList<Regalo>()
                            listaRegalos = regalosActualizados

                        } catch (e: Exception) {
                            println("Error al guardar regalo: ${e.message}")
                            e.printStackTrace()
                        }
                    }
                },
                enabled = nombreRegalo.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar Regalo")
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text("Regalos Guardados", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(listaRegalos) { regalo ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
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
}
