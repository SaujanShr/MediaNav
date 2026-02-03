package com.example.medianav.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.medianav.R
import com.example.medianav.ui.navigation.PluginViewModel
import com.example.medianav.ui.shared.ErrorBanner

@Composable
fun HomeScreen(
    pluginViewModel: PluginViewModel
) {
    val errorMessages = remember { mutableStateListOf<String>() }

    LaunchedEffect(pluginViewModel) {
        pluginViewModel.errors.collect { errorMessages.add(it) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            MediaNavLogo()
            CurrentPluginHeader(pluginViewModel)
            PluginTab(pluginViewModel)
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            errorMessages.forEach { message ->
                ErrorBanner(
                    message = message,
                    onDismiss = { errorMessages.remove(message) }
                )
            }
        }
    }
}

@Composable
private fun MediaNavLogo() {
    Image(
        painter = painterResource(R.drawable.ic_medianav_logo),
        contentDescription = null,
        modifier = Modifier.height(160.dp)
    )
}
