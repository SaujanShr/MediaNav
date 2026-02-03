package com.example.medianav.util

fun String.toTitleCase(): String =
    split('_').joinToString(" ") {
        it.lowercase().replaceFirstChar { ch -> ch.uppercase() }
    }