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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegaloDetailScreen(navController: NavController, regaloId: Long) {

    val isCreateMode = regaloId == -1L
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var nombreRegalo by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("") }
    var fotos by remember { mutableStateOf<List<FotoRegalo>>(emptyList()) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null && !isCreateMode) {
                scope.launch {
                    uploadPhotoRegalo(context, uri, regaloId, scope, snackbarHostState) { newPhoto ->
                        fotos = fotos + newPhoto
                    }
                }
            }
        }
    )

    fun loadData() {
        if (!isCreateMode) {
            scope.launch {
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
                    val photoResult = SupabaseCliente.client.postgrest
                        .from("fotos_regalos")
                        .select { filter { eq("regalo_id", regaloId) } }
                        .decodeList<FotoRegalo>()
                    fotos = photoResult
                } catch (e: Exception) {
                    scope.launch { snackbarHostState.showSnackbar("Error al cargar datos: ${e.message}") }
                }
            }
        }
    }

    LaunchedEffect(regaloId) {
        loadData()
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
                                SupabaseCliente.client.postgrest.from("regalos").delete { filter { eq("id", regaloId) } }
                                showDeleteDialog = false
                                navController.previousBackStackEntry?.savedStateHandle?.set("shouldRefresh", true)
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
                title = { Text(if (isCreateMode) "Añadir Nuevo Regalo" else "Editar Regalo") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                        photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Añadir Foto")
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            if (isCreateMode) {
                Button(
                    onClick = {
                        val nuevoRegalo = Regalo(nombreRegalo = nombreRegalo, fecha = fecha.ifEmpty { null }, descripcion = descripcion.ifEmpty { null }, tipo = tipo.ifEmpty { null })
                        scope.launch {
                            try {
                                SupabaseCliente.client.postgrest.from("regalos").insert(nuevoRegalo)
                                navController.popBackStack()
                            } catch (e: Exception) { 
                                scope.launch { snackbarHostState.showSnackbar("Error al guardar: ${e.message}") }
                            }
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
                            } catch (e: Exception) { 
                                scope.launch { snackbarHostState.showSnackbar("Error al actualizar: ${e.message}") }
                            }
                        }
                    },
                    enabled = nombreRegalo.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Actualizar")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { showDeleteDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Borrar")
                }
            }
        }
    }
}

suspend fun uploadPhotoRegalo(
    context: Context, 
    uri: Uri, 
    regaloId: Long, 
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    onComplete: (FotoRegalo) -> Unit
) {
    try {
        val fileBytes = context.contentResolver.openInputStream(uri)?.readBytes()
        if (fileBytes != null) {
            val fileName = "${UUID.randomUUID()}.jpg"
            val bucket = SupabaseCliente.client.storage.from("fotos_regalos")

            bucket.upload(fileName, fileBytes, upsert = false)

            val publicUrl = bucket.publicUrl(fileName)

            val newFoto = FotoRegalo(regaloId = regaloId, urlFoto = publicUrl)
            val savedFoto = SupabaseCliente.client.postgrest.from("fotos_regalos")
                .insert(newFoto)
                .decodeSingle<FotoRegalo>()
            
            onComplete(savedFoto)
        } else {
             scope.launch { snackbarHostState.showSnackbar("No se pudo leer la imagen.") }
        }
    } catch (e: Exception) {
        scope.launch { snackbarHostState.showSnackbar("Error al subir foto: ${e.message}") }
    }
}
