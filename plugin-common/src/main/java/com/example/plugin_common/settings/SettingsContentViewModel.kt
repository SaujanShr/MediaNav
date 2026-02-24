package com.example.plugin_common.settings

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.*

class SettingsContentViewModel : ViewModel() {
    private val _errors = MutableSharedFlow<String>()
    val errors = _errors.asSharedFlow()

    suspend fun emitError(message: String) {
        _errors.emit(message)
    }

    private val _expandedSettingIndex = MutableStateFlow<Int?>(null)
    val expandedSettingIndex: StateFlow<Int?> = _expandedSettingIndex.asStateFlow()

    fun toggleExpanded(settingIndex: Int) {
        _expandedSettingIndex.value =
            if (_expandedSettingIndex.value == settingIndex) null
            else settingIndex
    }
}