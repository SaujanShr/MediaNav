package com.example.medianav.ui.navigation

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import androidx.navigation.compose.rememberNavController
import com.example.medianav.ui.home.HomeScreen
import com.example.medianav.ui.library.LibraryScreen
import com.example.medianav.ui.settings.SettingsScreen

@Composable
fun MediaNavApplication() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                MediaNavDestination.entries.forEach { destination ->
                    NavBarItem(
                        navController = navController,
                        destination = destination,
                        currentDestination = currentDestination
                    )
                }
            }
        }
    ) { innerPadding ->
        AppNavHost(navController,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }
}

@Composable
private fun RowScope.NavBarItem(
    navController: NavController,
    destination: MediaNavDestination,
    currentDestination: NavDestination?
) {
    NavigationBarItem(
        selected = currentDestination?.route == destination.route,
        onClick = {
            navController.navigate(destination.route) {
                popUpTo(navController.graph.startDestinationId) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        },
        icon = {
            Icon(
                imageVector = destination.icon,
                contentDescription = null
            )
        },
    )
}

@Composable
private fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier
) {
    val pluginViewModel: PluginViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = MediaNavDestination.Home.route,
        modifier = modifier
    ) {
        composable(MediaNavDestination.Home.route) {
            HomeScreen(pluginViewModel = pluginViewModel)
        }
        composable(MediaNavDestination.Library.route) {
            LibraryScreen(
                pluginViewModel = pluginViewModel
            )
        }
        composable(MediaNavDestination.Settings.route) {
            SettingsScreen(pluginViewModel = pluginViewModel)
        }
    }
}
