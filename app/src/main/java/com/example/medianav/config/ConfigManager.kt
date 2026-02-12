package com.example.medianav.config

import android.content.Context
import com.example.medianav.ui.theme.Theme
import com.example.plugin_common.util.firstResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object ConfigManager {
    private lateinit var appContext: Context
    private val mutex = Mutex()
    private var initialized = false
    suspend fun initialize(context: Context) = mutex.withLock {
        if (initialized) return
        appContext = context.applicationContext
        restoreTheme()
        initialized = true
    }

    private val _theme = MutableStateFlow(Theme.DEFAULT)
    val theme = _theme.asStateFlow()

    private suspend fun restoreTheme() {
        _theme.value = ConfigStorage
            .getTheme(appContext)
            .firstResult()
            .getOrElse { Theme.DEFAULT }
    }

    suspend fun setTheme(newTheme: Theme) {
        ConfigStorage.setTheme(appContext, newTheme)
        _theme.value = newTheme
    }
}
