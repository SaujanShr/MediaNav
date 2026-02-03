package com.example.medianav

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.medianav.config.ConfigManager
import com.example.medianav.library.LibraryManager
import com.example.medianav.plugin.PluginManager
import com.example.medianav.ui.navigation.NavApplication
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
    val theme by ConfigManager.theme.collectAsState()

    MediaNavTheme(theme = theme) {
        Surface(modifier = Modifier.fillMaxSize()) {
            NavApplication()
        }
    }
}
