package com.adp.appsilvant.pantallas

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.adp.appsilvant.data.Viaje
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalDateTime

@Composable
fun PantallaPrincipal(navController: NavController) {

    var currentlyWatching by remember { mutableStateOf<List<MediaVisto>>(emptyList()) }
    var recentTrips by remember { mutableStateOf<List<Viaje>>(emptyList()) }

    // Load data for the carousels
    LaunchedEffect(Unit) {
        try {
            // Fetch currently watching media
            currentlyWatching = SupabaseCliente.client.postgrest
                .from("media_vistos")
                .select { filter { eq("estado", "viendo") } }
                .decodeList<MediaVisto>()

            // Fetch recent trips
            recentTrips = SupabaseCliente.client.postgrest
                .from("viajes")
                .select { order("creado_en", Order.DESCENDING); limit(10) }
                .decodeList<Viaje>()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .animateContentSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Counter Section
        item {
            ContadorSection()
        }

        // Currently Watching Carousel
        if (currentlyWatching.isNotEmpty()) {
            item {
                DashboardCarousel(
                    title = "Viendo Actualmente",
                    items = currentlyWatching,
                    itemContent = { media ->
                        MediaCarouselItem(media) { 
                            navController.navigate("media_detail/${media.id}") 
                        }
                    }
                )
            }
        }

        // Recent Trips Carousel
        if (recentTrips.isNotEmpty()) {
            item {
                DashboardCarousel(
                    title = "Últimos Viajes",
                    items = recentTrips,
                    itemContent = { viaje ->
                        ViajeCarouselItem(viaje) { 
                            navController.navigate("viaje_detail/${viaje.id}")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ContadorSection() {
    val fechaInicio = LocalDateTime.of(2025, 6, 7, 0, 0)
    val textoContador = calcularTiempoJuntos(fechaInicio)

    Column(
        modifier = Modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.silviaantonio),
            contentDescription = "Foto de la pareja",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(180.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Llevamos juntos",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = textoContador,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun <T> DashboardCarousel(
    title: String,
    items: List<T>,
    itemContent: @Composable (T) -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(items) {
                itemContent(it)
            }
        }
    }
}

@Composable
private fun MediaCarouselItem(media: MediaVisto, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column {
            AsyncImage(
                model = "https://image.tmdb.org/t/p/w500${media.posterPath}",
                contentDescription = media.titulo,
                contentScale = ContentScale.Crop,
                modifier = Modifier.height(200.dp)
            )
            Text(
                text = media.titulo,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
private fun ViajeCarouselItem(viaje: Viaje, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .height(120.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = viaje.lugar,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
private fun calcularTiempoJuntos(fechaInicio: LocalDateTime): String {
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
    return "$dias días\n$horas horas, $minutos minutos y $segundos segundos"
}
