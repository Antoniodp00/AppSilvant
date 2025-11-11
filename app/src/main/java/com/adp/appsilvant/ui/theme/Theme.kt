package com.adp.appsilvant.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color // *** THE MISSING IMPORT ***
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = MutedRed,
    secondary = SoftPink,
    tertiary = SoftPink,
    background = DarkWarmGray,
    surface = DarkSurface,
    onPrimary = Color.White,
    onSecondary = DarkWarmGray,
    onTertiary = DarkWarmGray,
    onBackground = CreamyWhite,
    onSurface = CreamyWhite,
    error = Color.Red
)

private val LightColorScheme = lightColorScheme(
    primary = WarmRed,
    secondary = SoftPink,
    tertiary = WarmRed,
    background = CreamyWhite,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = WarmRed,
    onTertiary = Color.White,
    onBackground = DarkWarmGray,
    onSurface = DarkWarmGray,
    error = Color.Red
)

@Composable
fun AppSilvantTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
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
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
