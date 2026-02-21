package com.example.medianav

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import com.example.medianav.config.ConfigManager
import com.example.medianav.library.LibraryManager
import com.example.medianav.plugin.PluginManager
import com.example.medianav.ui.navigation.MediaNavApplication
import com.example.medianav.ui.shared.LoadingScreen
import com.example.medianav.ui.theme.MediaNavTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        lifecycleScope.launch {
            PluginManager.initialize(this@MainActivity)
            ConfigManager.initialize(this@MainActivity)
            LibraryManager.initialize(this@MainActivity)
        }

        setContent {
            Application()
        }
    }
}

@Composable
private fun Application() {
    val context = LocalContext.current
    val theme by ConfigManager.theme.collectAsState()
    var initialized by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        PluginManager.initialize(context)
        ConfigManager.initialize(context)
        LibraryManager.initialize(context)
        initialized = true
    }

    MediaNavTheme(theme = theme) {
        Surface(modifier = Modifier.fillMaxSize()) {
            if (initialized) {
                MediaNavApplication()
            } else {
                LoadingScreen()
            }
        }
    }
}

