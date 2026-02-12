package com.example.medianav.plugin

import android.content.Context
import android.content.res.Resources
import android.content.res.loader.ResourcesLoader
import android.content.res.loader.ResourcesProvider
import android.os.ParcelFileDescriptor
import com.example.plugin_common.plugin.MediaPlugin
import dalvik.system.DexClassLoader
import java.io.File
import java.io.IOException
import java.lang.reflect.Modifier
import java.util.Properties

internal object PluginLoader {
    fun loadPlugin(context: Context, apkFile: File): Result<MediaPlugin> = runCatching {
        val safeApk = prepareApkForLoading(context, apkFile)
        val classLoader = createPluginClassLoader(context, safeApk)
        val pluginClassName = findPluginClassName(classLoader, safeApk)
        val pluginResources = createPluginResources(context, safeApk)

        instantiatePlugin(context, classLoader, pluginClassName, pluginResources)
    }

    private fun prepareApkForLoading(context: Context, apkFile: File): File {
        val safeName = apkFile.name.replace(Regex("[^a-zA-Z0-9._-]"), "_")
        val safeApk = File.createTempFile(
            PluginConstants.FilePrefixes.PLUGIN,
            "_$safeName",
            context.codeCacheDir
        )
        apkFile.copyTo(safeApk, overwrite = true)

        if (!safeApk.setReadOnly()) {
            throw IOException("Failed to set APK read-only: ${safeApk.name}")
        }
        return safeApk
    }

    private fun createPluginClassLoader(context: Context, apkFile: File): DexClassLoader {
        val optimizedDir = File(
            context.codeCacheDir,
            PluginConstants.Paths.ODEX_DIR
        ).apply { mkdirs() }

        return DexClassLoader(
            apkFile.absolutePath,
            optimizedDir.absolutePath,
            null,
            MediaPlugin::class.java.classLoader
        )
    }

    private fun findPluginClassName(classLoader: DexClassLoader, apkFile: File): String {
        val stream = classLoader.getResourceAsStream(
            PluginConstants.Keys.PLUGIN_PROPERTIES
        ) ?: throw IOException("Missing plugin.properties in ${apkFile.name}")

        val properties = Properties().apply {
            stream.use { load(it) }
        }

        return properties.getProperty(
            PluginConstants.Keys.PLUGIN_CLASS
        )?.trim() ?: throw IOException("PLUGIN_CLASS missing in ${apkFile.name}")
    }

    private fun instantiatePlugin(
        context: Context,
        classLoader: DexClassLoader,
        className: String,
        pluginResources: Resources
    ): MediaPlugin {
        val pluginClass = classLoader.loadClass(className)
        check(isValidPluginClass(pluginClass)) {
            "Class $className is not a valid MediaPlugin implementation"
        }

        val plugin = pluginClass.getDeclaredConstructor().newInstance() as MediaPlugin
        val dataDir = PluginStorage.pluginDataDir(context, plugin.metadata.id)
        plugin.resources.attach(dataDir, pluginResources)
        return plugin
    }

    private fun isValidPluginClass(clazz: Class<*>): Boolean =
        MediaPlugin::class.java.isAssignableFrom(clazz) &&
                !clazz.isInterface &&
                !Modifier.isAbstract(clazz.modifiers) &&
                !clazz.isAnonymousClass &&
                !clazz.isSynthetic &&
                clazz.enclosingClass == null

    private fun createPluginResources(context: Context, apkFile: File): Resources {
        val loader = ResourcesLoader()

        ParcelFileDescriptor.open(apkFile, ParcelFileDescriptor.MODE_READ_ONLY)
            .use { pfd ->
                val provider = ResourcesProvider.loadFromApk(pfd)
                loader.addProvider(provider)
            }

        val pluginContext = context.createConfigurationContext(
            context.resources.configuration
        )
        pluginContext.resources.addLoaders(loader)

        return pluginContext.resources
    }
}
