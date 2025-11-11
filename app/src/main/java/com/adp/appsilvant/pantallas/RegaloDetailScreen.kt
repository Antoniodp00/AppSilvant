package com.adp.appsilvant.pantallas

import androidx.compose.foundation.layout.*
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
fun RegaloDetailScreen(navController: NavController, regaloId: Long) {

    val isCreateMode = regaloId == -1L
    val scope = rememberCoroutineScope()

    var nombreRegalo by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("") }

    // Load existing data if in edit mode
    LaunchedEffect(regaloId) {
        if (!isCreateMode) {
            try {
                SupabaseCliente.client.postgrest
                    .from("regalos")
                    .select { filter { eq("id", regaloId) } }
                    .decodeSingleOrNull<Regalo>()?.let { regalo ->
                        nombreRegalo = regalo.nombreRegalo
                        fecha = regalo.fecha ?: ""
                        descripcion = regalo.descripcion ?: ""
                        tipo = regalo.tipo ?: ""
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isCreateMode) "Añadir Nuevo Regalo" else "Editar Regalo") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = nombreRegalo,
                onValueChange = { nombreRegalo = it },
                label = { Text("Nombre del Regalo (Obligatorio)") },
                modifier = Modifier.fillMaxWidth(),
                isError = nombreRegalo.isBlank()
            )
            OutlinedTextField(
                value = fecha,
                onValueChange = { fecha = it },
                label = { Text("Fecha (AAAA-MM-DD)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = tipo,
                onValueChange = { tipo = it },
                label = { Text("Tipo (ej. regalado/recibido)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isCreateMode) {
                Button(
                    onClick = {
                        val nuevoRegalo = Regalo(nombreRegalo = nombreRegalo, fecha = fecha.ifEmpty { null }, descripcion = descripcion.ifEmpty { null }, tipo = tipo.ifEmpty { null })
                        scope.launch {
                            try {
                                SupabaseCliente.client.postgrest.from("regalos").insert(nuevoRegalo)
                                navController.popBackStack()
                            } catch (e: Exception) { e.printStackTrace() }
                        }
                    },
                    enabled = nombreRegalo.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Guardar")
                }
            } else {
                Button(
                    onClick = {
                        val updates = mapOf("nombre_regalo" to nombreRegalo, "fecha" to fecha.ifEmpty { null }, "descripcion" to descripcion.ifEmpty { null }, "tipo" to tipo.ifEmpty { null })
                        scope.launch {
                            try {
                                SupabaseCliente.client.postgrest.from("regalos").update(updates) { 
                                    filter {
                                        eq("id", regaloId) 
                                    }
                                }
                                navController.popBackStack()
                            } catch (e: Exception) { e.printStackTrace() }
                        }
                    },
                    enabled = nombreRegalo.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Actualizar")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        scope.launch {
                            try {
                                SupabaseCliente.client.postgrest.from("regalos").delete { 
                                    filter {
                                        eq("id", regaloId) 
                                    }
                                }
                                navController.popBackStack()
                            } catch (e: Exception) { e.printStackTrace() }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Borrar")
                }
            }
        }
    }
}