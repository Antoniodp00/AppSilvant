package com.adp.appsilvant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.adp.appsilvant.pantallas.*
import com.adp.appsilvant.ui.theme.AppSilvantTheme
import kotlinx.coroutines.launch

// --- Navigation Definitions ---
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Inicio", Icons.Filled.Home)
    object Viajes : Screen("viajes", "Viajes", Icons.Filled.TravelExplore)
    object Regalos : Screen("regalos", "Regalos", Icons.Filled.CardGiftcard)
    object Media : Screen("media", "Media", Icons.Filled.Movie)
}

val mainScreens = listOf(Screen.Home, Screen.Viajes, Screen.Regalos, Screen.Media)

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppSilvantTheme {
                val navController = rememberNavController()
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        AppDrawer(navController, mainScreens) {
                            scope.launch { drawerState.close() }
                        }
                    },
                ) {
                    Scaffold(
                        topBar = {
                            if (mainScreens.any { it.route == currentRoute }) {
                                TopAppBar(
                                    title = {
                                        val title = mainScreens.find { it.route == currentRoute }?.title ?: "AppSilvant"
                                        Text(title)
                                    },
                                    navigationIcon = {
                                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                            Icon(Icons.Default.Menu, contentDescription = "Abrir menú")
                                        }
                                    }
                                )
                            }
                        }
                    ) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = Screen.Home.route,
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            composable(Screen.Home.route) { PantallaPrincipal(navController) }
                            composable(Screen.Viajes.route) { ViajesScreen(navController) }
                            composable(Screen.Regalos.route) { RegalosScreen(navController) }
                            composable(Screen.Media.route) { MediaScreen(navController) }

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
                            composable(
                                route = "media_detail/{mediaId}",
                                arguments = listOf(navArgument("mediaId") { type = NavType.LongType })
                            ) {
                                val mediaId = it.arguments?.getLong("mediaId") ?: -1L
                                MediaDetailScreen(navController = navController, mediaId = mediaId)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppDrawer(
    navController: NavHostController,
    screens: List<Screen>,
    onItemClick: () -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    ModalDrawerSheet {
        Spacer(Modifier.height(12.dp))
        screens.forEach { screen ->
            NavigationDrawerItem(
                icon = { Icon(screen.icon, contentDescription = screen.title) },
                label = { Text(screen.title) },
                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                    onItemClick()
                },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }
    }
}
