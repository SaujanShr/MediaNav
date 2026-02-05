package com.example.plugin_common.plugin.info

enum class PluginCategory(val value: String) {
    ART("Art"),
    GAME("Game"),
    LEAK("Leak"),
    VIDEO("Video"),
    UNKNOWN("Unknown");

    companion object {
        private val map = entries.associateBy(PluginCategory::value)

        fun fromValue(value: String?) = map[value] ?: UNKNOWN
    }
}