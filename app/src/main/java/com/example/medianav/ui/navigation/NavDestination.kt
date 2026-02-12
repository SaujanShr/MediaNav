package com.example.medianav.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector


internal enum class NavDestination(val route: String, val icon: ImageVector) {
    Home("home", Icons.Default.Home),
    Library("library", Icons.Default.LocalLibrary),
    Settings("settings", Icons.Default.Settings),
    Media("media", Icons.Default.PlayArrow)
}
