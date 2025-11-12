package com.adp.appsilvant.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Paleta para Modo OSCURO
private val DarkColorScheme = darkColorScheme(
    primary = RojoPrincipalOscuro,
    secondary = RojoVarianteOscuro,
    tertiary = AcentoSuaveOscuro,
    background = FondoOscuro,
    surface = FondoOscuro,
    onPrimary = TextoPrimarioOscuro,
    onSecondary = TextoPrimarioClaro,
    onTertiary = TextoPrimarioOscuro,
    onBackground = TextoPrimarioOscuro,
    onSurface = TextoPrimarioOscuro,
    error = Color(0xFFCF6679) // Un rojo de error estándar para modo oscuro
)

// Paleta para Modo CLARO
private val LightColorScheme = lightColorScheme(
    primary = RojoPrincipal,
    secondary = RojoVariante,
    tertiary = AcentoSuave,
    background = FondoClaro,
    surface = FondoClaro,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = TextoPrimarioClaro,
    onBackground = TextoPrimarioClaro,
    onSurface = TextoPrimarioClaro,
    error = Color(0xFFB00020) // Un rojo de error estándar
)

@Composable
fun AppSilvantTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color NO está disponible en tu app (targetApi=31)
    // Lo ponemos a 'false' para tener control total
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        // Aquí usamos la tipografía Nunito que ya definiste
        typography = Typography,
        content = content
    )
}