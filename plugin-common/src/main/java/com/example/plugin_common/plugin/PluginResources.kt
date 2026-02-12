package com.example.plugin_common.plugin

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

data class PluginResources(
    private val iconResId: Int,
    private val bannerResId: Int
) {
    private lateinit var resources: Resources
    private lateinit var dataDir: File

    @get:Composable
    val icon: Painter get() = remember(iconResId) {
        createPainter(iconResId)
    }

    @get:Composable
    val banner: Painter get() = remember(bannerResId) {
        createPainter(bannerResId)
    }

    private fun createPainter(resId: Int): Painter {
        return try {
            val drawable = ResourcesCompat.getDrawable(resources, resId, null)
            val bitmap = drawable?.toBitmap()
            if (bitmap != null) BitmapPainter(bitmap.asImageBitmap())
            else fallbackPainter()
        } catch (_: Exception) {
            fallbackPainter()
        }
    }

    private fun fallbackPainter(): Painter {
        val bitmap = createBitmap(1, 1)
        return BitmapPainter(bitmap.asImageBitmap())
    }

    fun attach(dataDir: File, resources: Resources) {
        this.dataDir = dataDir
        this.resources = resources
    }

    fun addDataFile(file: File) {
        if (!::dataDir.isInitialized) throw IllegalStateException("PluginResources not attached")
        file.copyTo(File(dataDir, file.name), overwrite = true)
    }

    fun removeDataFile(file: File) {
        if (!::dataDir.isInitialized) throw IllegalStateException("PluginResources not attached")
        file.delete()
    }

    fun listDataFiles(): Array<File> {
        if (!::dataDir.isInitialized) throw IllegalStateException("PluginResources not attached")
        return dataDir.listFiles() ?: emptyArray()
    }
}