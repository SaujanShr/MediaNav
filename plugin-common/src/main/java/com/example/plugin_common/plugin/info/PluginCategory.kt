package com.example.plugin_common.plugin.info

enum class PluginCategory(val value: String) {
    ART("art"),
    GAME("game"),
    LEAK("leak"),
    VIDEO("video"),
    UNKNOWN("unknown");

    companion object {
        private val map = entries.associateBy(PluginCategory::value)

        fun fromValue(value: String?) = map[value] ?: UNKNOWN
    }
}