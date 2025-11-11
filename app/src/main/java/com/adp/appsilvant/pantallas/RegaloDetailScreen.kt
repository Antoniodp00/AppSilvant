package com.adp.appsilvant.pantallas

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import com.adp.appsilvant.data.FotoRegalo
import com.adp.appsilvant.data.Regalo
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegaloDetailScreen(navController: NavController, regaloId: Long) {

    val isCreateMode = regaloId == -1L
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var nombreRegalo by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("") }
    var fotos by remember { mutableStateOf<List<FotoRegalo>>(emptyList()) }

    // --- Photo Picker Launcher ---
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null && !isCreateMode) {
                scope.launch {
                    uploadPhotoRegalo(context, uri, regaloId) { newPhoto ->
                        fotos = fotos + newPhoto
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
                    // Load gift details
                    SupabaseCliente.client.postgrest
                        .from("regalos")
                        .select { filter { eq("id", regaloId) } }
                        .decodeSingleOrNull<Regalo>()?.let { regalo ->
                            nombreRegalo = regalo.nombreRegalo
                            fecha = regalo.fecha ?: ""
                            descripcion = regalo.descripcion ?: ""
                            tipo = regalo.tipo ?: ""
                        }
                    // Load associated photos
                    val photoResult = SupabaseCliente.client.postgrest
                        .from("fotos_regalos")
                        .select { filter { eq("regalo_id", regaloId) } }
                        .decodeList<FotoRegalo>()
                    fotos = photoResult
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    LaunchedEffect(regaloId) {
        loadData()
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
                                    filter { eq("id", regaloId) }
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
                                    filter { eq("id", regaloId) }
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
suspend fun uploadPhotoRegalo(context: Context, uri: Uri, regaloId: Long, onComplete: (FotoRegalo) -> Unit) {
    try {
        val fileBytes = context.contentResolver.openInputStream(uri)?.readBytes()
        if (fileBytes != null) {
            val fileName = "${UUID.randomUUID()}.jpg"
            val bucket = SupabaseCliente.client.storage.from("fotos_regalos") // Correct bucket

            // 1. Upload the file
            bucket.upload(fileName, fileBytes, upsert = false)

            // 2. Get the public URL
            val publicUrl = bucket.publicUrl(fileName)

            // 3. Save the URL to PostgREST
            val newFoto = FotoRegalo(regaloId = regaloId, urlFoto = publicUrl)
            val savedFoto = SupabaseCliente.client.postgrest.from("fotos_regalos") // Correct table
                .insert(newFoto)
                .decodeSingle<FotoRegalo>()
            
            onComplete(savedFoto)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
