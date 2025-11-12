package com.adp.appsilvant.pantallas

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.adp.appsilvant.R
import com.adp.appsilvant.SupabaseCliente
import com.adp.appsilvant.data.MediaVisto
import com.adp.appsilvant.data.Regalo
import com.adp.appsilvant.data.TimelineItem // <-- 1. IMPORTAR
import com.adp.appsilvant.data.Viaje
import com.adp.appsilvant.utils.parseDate // <-- 2. IMPORTAR
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import io.github.jan.supabase.postgrest.query.Columns

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun PantallaPrincipal(navController: NavController) {

    val fechaInicio = LocalDateTime.of(2020, 10, 25, 18, 0)
    val textoContador = calcularTiempoJuntos(fechaInicio)
    val scope = rememberCoroutineScope()

    // Estados para las listas
    var listaViendo by remember { mutableStateOf<List<MediaVisto>>(emptyList()) }
    // --- 3. NUEVO ESTADO PARA LA LÍNEA DE TIEMPO ---
    var timelineItems by remember { mutableStateOf<List<TimelineItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        scope.launch {
            isLoading = true
            try {
                // --- 4. CARGAMOS TODO ---

                val viajes = SupabaseCliente.client.postgrest["viajes"]
                    .select(Columns.raw("*, fotos_viajes(url_foto, descripcion_foto)"))
                    .decodeList<Viaje>()

                val regalos = SupabaseCliente.client.postgrest["regalos"]
                    .select(Columns.raw("*, fotos_regalos(url_foto, descripcion_foto)"))
                    .decodeList<Regalo>()

                // --- 5. COMBINAMOS Y ORDENAMOS ---
                val viajesItems = viajes.map {
                    TimelineItem.ViajeItem(it, parseDate(it.fecha))
                }
                val regalosItems = regalos.map {
                    TimelineItem.RegaloItem(it, parseDate(it.fecha))
                }

                // Juntamos ambas listas, filtramos las que no tienen fecha y ordenamos
                timelineItems = (viajesItems + regalosItems)
                    .filter { it.fecha != null }
                    .sortedByDescending { it.fecha }

            } catch (e: Exception) {
                e.printStackTrace()
            }
            isLoading = false
        }
    }

    // --- 6. ACTUALIZAMOS LA UI ---
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- El Contador (Header) ---
        item {
            Spacer(modifier = Modifier.height(32.dp))
            Image(
                painter = painterResource(id = R.drawable.silviaantonio),
                contentDescription = "Foto de la pareja",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Llevamos juntos",
                fontSize = 22.sp,
                fontFamily = com.adp.appsilvant.ui.theme.Nunito,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = textoContador,
                fontSize = 26.sp,
                fontFamily = com.adp.appsilvant.ui.theme.Nunito,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))
            Divider()
        }

        // --- Carrusel "Viendo Actualmente" (sigue igual) ---
        if (listaViendo.isNotEmpty()) {
            item {
                Text(
                    text = "Viendo actualmente",
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = com.adp.appsilvant.ui.theme.Nunito,
                    modifier = Modifier.padding(vertical = 16.dp).fillMaxWidth()
                )
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(listaViendo) { media ->
                        MediaItemCard(media, navController)
                    }
                }
            }
        }

        // --- 7. NUEVA LÍNEA DE TIEMPO ---
        item {
            Text(
                text = "Nuestra Línea de Tiempo",
                style = MaterialTheme.typography.titleLarge,
                fontFamily = com.adp.appsilvant.ui.theme.Nunito,
                modifier = Modifier.padding(vertical = 16.dp).fillMaxWidth()
            )
        }

        if (isLoading) {
            item {
                CircularProgressIndicator(modifier = Modifier.padding(vertical = 32.dp))
            }
        } else if (timelineItems.isEmpty()) {
            item {
                Text(
                    text = "Aún no hay viajes ni regalos guardados.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(32.dp)
                )
            }
        } else {
            // Mostramos la lista combinada
            items(timelineItems) { item ->
                when (item) {
                    is TimelineItem.ViajeItem -> TimelineViajeCard(item.viaje, navController)
                    is TimelineItem.RegaloItem -> TimelineRegaloCard(item.regalo, navController)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Espacio al final
        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

// --- 8. NUEVOS COMPOSABLES PARA LAS TARJETAS ---
@Composable
fun TimelineViajeCard(viaje: Viaje, navController: NavController) {
    val fotoUrl = viaje.fotos.firstOrNull()?.urlFoto

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate("viaje_detail/${viaje.id}") }
    ) {
        if (fotoUrl != null) {
            AsyncImage(
                model = fotoUrl,
                contentDescription = viaje.lugar,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )
        }
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Viaje: ${viaje.lugar}",
                style = MaterialTheme.typography.titleMedium,
                fontFamily = com.adp.appsilvant.ui.theme.Nunito,
                fontWeight = FontWeight.Bold
            )
            viaje.fecha?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = com.adp.appsilvant.ui.theme.Nunito
                )
            }
        }
    }
}

@Composable
fun TimelineRegaloCard(regalo: Regalo, navController: NavController) {
    val fotoUrl = regalo.fotos.firstOrNull()?.urlFoto

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate("regalo_detail/${regalo.id}") }
    ) {
        if (fotoUrl != null) {
            AsyncImage(
                model = fotoUrl,
                contentDescription = regalo.nombreRegalo,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )
        }
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Regalo: ${regalo.nombreRegalo}",
                style = MaterialTheme.typography.titleMedium,
                fontFamily = com.adp.appsilvant.ui.theme.Nunito,
                fontWeight = FontWeight.Bold
            )
            regalo.fecha?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = com.adp.appsilvant.ui.theme.Nunito
                )
            }
        }
    }
}


// (El resto de composables: MediaItemCard, calcularTiempoJuntos, etc. se quedan igual)
@Composable
fun MediaItemCard(media: MediaVisto, navController: NavController) {
    OutlinedCard(
        modifier = Modifier
            .size(width = 140.dp, height = 240.dp)
            .clickable { navController.navigate("media_detail/${media.id}") }
    ) {
        Column {
            AsyncImage(
                model = "https://image.tmdb.org/t/p/w500${media.posterPath}",
                contentDescription = media.titulo,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )
            Text(
                text = media.titulo,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = com.adp.appsilvant.ui.theme.Nunito,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
fun calcularTiempoJuntos(fechaInicio: LocalDateTime): String {
    var textoContador by remember { mutableStateOf(obtenerTextoDuracion(fechaInicio)) }

    LaunchedEffect(true) {
        while (true) {
            textoContador = obtenerTextoDuracion(fechaInicio)
            delay(1000)
        }
    }
    return textoContador
}

private fun obtenerTextoDuracion(inicio: LocalDateTime): String {
    val ahora = LocalDateTime.now()
    val duracion = Duration.between(inicio, ahora)
    val dias = duracion.toDays()
    val horas = duracion.toHours() % 24
    val minutos = duracion.toMinutes() % 60
    val segundos = duracion.toSeconds() % 60
    return "$dias días, $horas horas, $minutos min y $segundos seg"
}