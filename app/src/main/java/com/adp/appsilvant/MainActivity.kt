package com.adp.appsilvant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.adp.appsilvant.pantallas.MediaDetailScreen
import com.adp.appsilvant.pantallas.MediaScreen
import com.adp.appsilvant.pantallas.RegaloDetailScreen
import com.adp.appsilvant.pantallas.RegalosScreen
import com.adp.appsilvant.pantallas.ViajeDetailScreen
import com.adp.appsilvant.pantallas.ViajesScreen
import com.adp.appsilvant.ui.theme.AppSilvantTheme
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalDateTime

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppSilvantTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "pantalla_principal") {
                        composable("pantalla_principal") {
                            PantallaPrincipal(navController = navController)
                        }
                        composable("viajes") {
                            ViajesScreen(navController = navController)
                        }
                        composable("regalos") {
                            RegalosScreen(navController = navController)
                        }
                        composable("media") {
                            MediaScreen(navController = navController)
                        }
                        composable(
                            route = "media_detail/{mediaId}",
                            arguments = listOf(navArgument("mediaId") { type = NavType.LongType })
                        ) {
                            val mediaId = it.arguments?.getLong("mediaId") ?: -1L
                            MediaDetailScreen(navController = navController, mediaId = mediaId)
                        }
                        composable(
                            route = "viaje_detail/{viajeId}",
                            arguments = listOf(navArgument("viajeId") { type = NavType.LongType })
                        ) {
                            val viajeId = it.arguments?.getLong("viajeId") ?: -1L
                            ViajeDetailScreen(navController = navController, viajeId = viajeId)
                        }
                        composable(
                            route = "regalo_detail/{regaloId}",
                            arguments = listOf(navArgument("regaloId") { type = NavType.LongType })
                        ) {
                            val regaloId = it.arguments?.getLong("regaloId") ?: -1L
                            RegaloDetailScreen(navController = navController, regaloId = regaloId)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PantallaPrincipal(navController: NavController) {

    val fechaInicio = LocalDateTime.of(2025, 6, 7, 0, 0)
    val textoContador = calcularTiempoJuntos(fechaInicio)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .animateContentSize(), // Subtle animation
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
            fontSize = 20.sp,
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

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedButton(onClick = { navController.navigate("viajes") }) {
            Text("Nuestros Viajes")
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(onClick = { navController.navigate("regalos") }) {
            Text("Nuestros Regalos")
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(onClick = { navController.navigate("media") }) {
            Text("Series y Películas")
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
    return "$dias días\n$horas horas, $minutos minutos y $segundos segundos"
}

@Preview(showBackground = true)
@Composable
fun PreviewPantallaPrincipal() {
    AppSilvantTheme {
        PantallaPrincipal(navController = rememberNavController())
    }
}
