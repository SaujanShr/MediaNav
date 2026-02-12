package com.example.plugin_common.plugin

import kotlin.collections.get

enum class PluginCategory(val value: String) {
    AUDIO("Audio"),
    BOOK("Book"),
    GAME("Game"),
    IMAGE("Image"),
    VIDEO("Video"),
    UNKNOWN("Unknown");

    companion object {
        private val map = entries.associateBy(PluginCategory::value)

        fun fromValue(value: String?) = map[value] ?: UNKNOWN
    }
}