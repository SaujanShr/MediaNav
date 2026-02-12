package com.example.plugin_common.util

fun String.toTitleCase(): String =
    split('_').joinToString(" ") {
        it.lowercase().replaceFirstChar { ch -> ch.uppercase() }
    }