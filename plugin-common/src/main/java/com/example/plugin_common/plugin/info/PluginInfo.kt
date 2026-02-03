package com.example.plugin_common.plugin.info

import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toBitmap
import java.io.File

data class PluginInfo(
    val id: String,
    val name: String,
    val version: String,
    val category: PluginCategory,
    private val iconResId: Int
) {
    private lateinit var resources: Resources
    private lateinit var dataDir: File

    @get:Composable
    val icon: Painter get() = remember(iconResId) {
        createPainter(iconResId)
    }

    private fun createPainter(resId: Int): Painter {
        return try {
            val drawable = ResourcesCompat.getDrawable(resources, resId, null)
            val bitmap = drawable?.toBitmap()
            if (bitmap != null) BitmapPainter(bitmap.asImageBitmap())
            else fallbackIcon()
        } catch (_: Exception) {
            fallbackIcon()
        }
    }

    private fun fallbackIcon(): Painter {
        val bitmap = createBitmap(1, 1)
        return BitmapPainter(bitmap.asImageBitmap())
    }

    fun attach(dataDir: File, resources: Resources) {
        this.dataDir = dataDir
        this.resources = resources
    }
}
