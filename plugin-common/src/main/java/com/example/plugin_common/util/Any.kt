package com.example.plugin_common.util

inline fun <T> T.conditionally(
    condition: Boolean,
    transform: (T) -> T
): T = if (condition) transform(this) else this
