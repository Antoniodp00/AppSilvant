package com.adp.appsilvant.pantallas

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest // CORRECT IMPORT PROVIDED BY YOU
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.adp.appsilvant.SupabaseCliente
import com.adp.appsilvant.data.FotoViaje
import com.adp.appsilvant.data.Viaje
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViajeDetailScreen(navController: NavController, viajeId: Long) {

    val isCreateMode = viajeId == -1L
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var lugar by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var fotos by remember { mutableStateOf<List<FotoViaje>>(emptyList()) }

    // --- Photo Picker Launcher ---
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null && !isCreateMode) {
                scope.launch {
                    uploadPhoto(context, uri, viajeId) { newPhoto ->
                        fotos = fotos + newPhoto // Add the new photo to the list
                    }
                }
            }
        }
    )

    // --- Data Loading ---
    fun loadData() {
        if (!isCreateMode) {
            scope.launch {
                try {
                    // Load trip details
                    SupabaseCliente.client.postgrest
                        .from("viajes")
                        .select { filter { eq("id", viajeId) } }
                        .decodeSingleOrNull<Viaje>()?.let { viaje ->
                            lugar = viaje.lugar
                            fecha = viaje.fecha ?: ""
                            descripcion = viaje.descripcion ?: ""
                        }
                    // Load associated photos
                    val photoResult = SupabaseCliente.client.postgrest
                        .from("fotos_viajes")
                        .select { filter { eq("viaje_id", viajeId) } }
                        .decodeList<FotoViaje>()
                    fotos = photoResult
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    LaunchedEffect(viajeId) {
        loadData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isCreateMode) "Añadir Nuevo Viaje" else "Editar Viaje") },
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
                value = lugar,
                onValueChange = { lugar = it },
                label = { Text("Lugar (Obligatorio)") },
                modifier = Modifier.fillMaxWidth(),
                isError = lugar.isBlank()
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

            Spacer(modifier = Modifier.height(16.dp))

            // --- Photo Gallery and Upload Button (only in edit mode) ---
            if (!isCreateMode) {
                Text("Galería", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(fotos) { foto ->
                        AsyncImage(
                            model = foto.urlFoto,
                            contentDescription = foto.descripcionFoto,
                            modifier = Modifier.height(100.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { 
                        // CORRECTED as per your instruction
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        ) 
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Añadir Foto")
                }
            }
            
            Spacer(modifier = Modifier.weight(1f)) // Pushes buttons to the bottom

            // --- Action Buttons ---
            if (isCreateMode) {
                Button(
                    onClick = {
                        val nuevoViaje = Viaje(lugar = lugar, fecha = fecha.ifEmpty { null }, descripcion = descripcion.ifEmpty { null })
                        scope.launch {
                            try {
                                SupabaseCliente.client.postgrest.from("viajes").insert(nuevoViaje)
                                navController.popBackStack()
                            } catch (e: Exception) { e.printStackTrace() }
                        }
                    },
                    enabled = lugar.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Guardar")
                }
            } else {
                Button(
                    onClick = {
                        val updates = mapOf("lugar" to lugar, "fecha" to fecha.ifEmpty { null }, "descripcion" to descripcion.ifEmpty { null })
                        scope.launch {
                            try {
                                SupabaseCliente.client.postgrest.from("viajes").update(updates) { 
                                    filter { eq("id", viajeId) }
                                }
                                navController.popBackStack()
                            } catch (e: Exception) { e.printStackTrace() }
                        }
                    },
                    enabled = lugar.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Actualizar")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        scope.launch {
                            try {
                                SupabaseCliente.client.postgrest.from("viajes").delete { 
                                    filter { eq("id", viajeId) }
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

// --- Helper function for photo upload logic ---
suspend fun uploadPhoto(context: Context, uri: Uri, viajeId: Long, onComplete: (FotoViaje) -> Unit) {
    try {
        val fileBytes = context.contentResolver.openInputStream(uri)?.readBytes()
        if (fileBytes != null) {
            val fileName = "${UUID.randomUUID()}.jpg"
            val bucket = SupabaseCliente.client.storage.from("fotos_viajes")

            // 1. Upload the file
            bucket.upload(fileName, fileBytes, upsert = false)

            // 2. Get the public URL
            val publicUrl = bucket.publicUrl(fileName)

            // 3. Save the URL to PostgREST
            val newFoto = FotoViaje(viajeId = viajeId, urlFoto = publicUrl)
            val savedFoto = SupabaseCliente.client.postgrest.from("fotos_viajes")
                .insert(newFoto)
                .decodeSingle<FotoViaje>()
            
            onComplete(savedFoto)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
