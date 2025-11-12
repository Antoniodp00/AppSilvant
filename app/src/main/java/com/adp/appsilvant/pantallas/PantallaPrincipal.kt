package com.adp.appsilvant.pantallas

import androidx.compose.animation.animateContentSize
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
import com.adp.appsilvant.data.Viaje
import com.adp.appsilvant.data.TimelineItem
import com.adp.appsilvant.data.TimelineType
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Duration
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay

@Composable
fun PantallaPrincipal(navController: NavController) {

    var timelineItems by remember { mutableStateOf<List<TimelineItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Cargar viajes y regalos y combinarlos en una línea de tiempo
    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val viajes = SupabaseCliente.client.postgrest
                .from("viajes")
                .select(columns = Columns.raw("*, fotos_viajes(url_foto,limit=1)"))
                .decodeList<Viaje>()

            val regalos = SupabaseCliente.client.postgrest
                .from("regalos")
                .select(columns = Columns.raw("*, fotos_regalos(url_foto,limit=1)"))
                .decodeList<Regalo>()

            val viajeItems = viajes.mapNotNull { v ->
                val date = parseLocalDate(v.fecha)
                date?.let { TimelineItem(type = TimelineType.VIAJE, fecha = it, viaje = v) }
            }
            val regaloItems = regalos.mapNotNull { r ->
                val date = parseLocalDate(r.fecha)
                date?.let { TimelineItem(type = TimelineType.REGALO, fecha = it, regalo = r) }
            }
            timelineItems = (viajeItems + regaloItems).sortedByDescending { it.fecha }
        } catch (e: Exception) {
            e.printStackTrace()
            timelineItems = emptyList()
        } finally {
            isLoading = false
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .animateContentSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Sección superior del contador
        item {
            ContadorSection()
        }

        // Cuerpo de la línea de tiempo
        item { Spacer(modifier = Modifier.height(8.dp)) }
        if (isLoading) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        } else if (timelineItems.isEmpty()) {
            item {
                Text(
                    text = "No hay elementos todavía.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(32.dp)
                )
            }
        } else {
            items(timelineItems) { item ->
                TimelineCard(item = item, onClick = {
                    when (item.type) {
                        TimelineType.VIAJE -> item.viaje?.let { navController.navigate("viaje_detail/${it.id}") }
                        TimelineType.REGALO -> item.regalo?.let { navController.navigate("regalo_detail/${it.id}") }
                    }
                })
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
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
private fun TimelineCard(item: TimelineItem, onClick: () -> Unit) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick)
    ) {
        when (item.type) {
            TimelineType.VIAJE -> {
                val viaje = item.viaje!!
                val portada = viaje.fotos.firstOrNull()?.urlFoto
                AsyncImage(
                    model = portada,
                    contentDescription = "Foto de portada del viaje",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentScale = ContentScale.Crop
                )
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(viaje.lugar, style = MaterialTheme.typography.titleMedium)
                    viaje.fecha?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                }
            }
            TimelineType.REGALO -> {
                val regalo = item.regalo!!
                val portada = regalo.fotos.firstOrNull()?.urlFoto
                AsyncImage(
                    model = portada,
                    contentDescription = "Foto de portada del regalo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentScale = ContentScale.Crop
                )
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(regalo.nombreRegalo, style = MaterialTheme.typography.titleMedium)
                    regalo.fecha?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                }
            }
        }
    }
}

private fun parseLocalDate(fechaStr: String?): LocalDate? {
    if (fechaStr.isNullOrBlank()) return null
    val patterns = listOf(
        "yyyy-MM-dd",
        "dd/MM/yyyy",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd HH:mm:ss"
    )
    for (p in patterns) {
        try {
            val fmt = DateTimeFormatter.ofPattern(p)
            return when (p) {
                "yyyy-MM-dd" , "dd/MM/yyyy" -> LocalDate.parse(fechaStr, fmt)
                else -> LocalDate.parse(fechaStr.substring(0, 10))
            }
        } catch (_: Exception) { /* try next */ }
    }
    return try {
        LocalDate.parse(fechaStr)
    } catch (_: Exception) {
        null
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
    val segundos = (duracion.seconds % 60)
    return "$dias días\n$horas horas, $minutos minutos y $segundos segundos"
}
