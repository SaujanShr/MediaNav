package com.example.medianav.ui.navigation

import android.content.pm.ActivityInfo
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
import com.example.medianav.ui.settings.SettingsScreen
import com.example.medianav.ui.shared.LockScreenOrientation


@Composable
fun NavApplication() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Lock orientation to portrait for all screens except Media
    val shouldLockOrientation = currentDestination?.route != NavDestination.Media.route
    if (shouldLockOrientation) {
        LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
    } else {
        LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
    }

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
                pluginViewModel = pluginViewModel
            )
        }
        composable(NavDestination.Settings.route) {
            SettingsScreen(pluginViewModel = pluginViewModel)
        }
    }
}
