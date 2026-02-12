package com.example.medianav.plugin

internal object PluginConstants {
    const val DATA_STORE_NAME = "plugins"

    object Paths {
        const val DATA_DIR = "plugins/data"
        const val APK_DIR = "plugins/apk"
        const val ODEX_DIR = "plugin_odex"
    }

    object Keys {
        const val INSTALLED_PLUGINS = "installed_plugins"
        const val ENABLED_PLUGINS = "enabled_plugins"
        const val SECRETS = "plugin_secrets"
        const val PLUGIN_PROPERTIES = "META-INF/plugin.properties"
        const val PLUGIN_CLASS = "pluginClass"
    }

    object FilePrefixes {
        const val PLUGIN = "plugin_"
    }
}
