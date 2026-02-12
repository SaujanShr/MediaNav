package com.example.medianav.ui.media

import androidx.lifecycle.ViewModel
import com.example.plugin_common.library.LibraryItem
import com.example.plugin_common.plugin.MediaPlugin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MediaViewModel : ViewModel() {
    private val _selectedItem = MutableStateFlow<LibraryItem?>(null)
    val selectedItem = _selectedItem.asStateFlow()

    private val _currentPlugin = MutableStateFlow<MediaPlugin?>(null)
    val currentPlugin = _currentPlugin.asStateFlow()

    fun setMedia(item: LibraryItem, plugin: MediaPlugin) {
        _selectedItem.value = item
        _currentPlugin.value = plugin
    }
}
