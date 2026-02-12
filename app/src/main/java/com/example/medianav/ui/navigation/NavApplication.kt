package com.example.medianav.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import androidx.navigation.compose.rememberNavController
import com.example.medianav.ui.home.HomeScreen
import com.example.medianav.ui.library.LibraryScreen
import com.example.medianav.ui.media.MediaScreen
import com.example.medianav.ui.settings.SettingsScreen


@Composable
fun NavApplication() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            if (currentDestination?.route != NavDestination.Media.route) {
                NavigationBar {
                    NavDestination.entries.filter { it != NavDestination.Media }.forEach { destination ->
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
private fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier
) {
    val pluginViewModel: PluginViewModel = viewModel()
    val itemViewModel: ItemViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = NavDestination.Home.route,
        modifier = modifier
    ) {
        composable(NavDestination.Home.route) {
            HomeScreen(pluginViewModel = pluginViewModel)
        }
        composable(NavDestination.Library.route) {
            LibraryScreen(
                pluginViewModel = pluginViewModel,
                onItemClick = { item, plugin ->
                    itemViewModel.setItem(item, plugin)
                    navController.navigate(NavDestination.Media.route)
                }
            )
        }
        composable(NavDestination.Settings.route) {
            SettingsScreen(pluginViewModel = pluginViewModel)
        }
        composable(NavDestination.Media.route) {
            MediaScreen(viewModel = itemViewModel)
        }
    }
}
