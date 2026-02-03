package com.example.plugin_common.plugin

interface SecretPlugin: MediaPlugin {
    val secretKeys: List<String>
    fun setSecrets(secrets: Map<String, String>)
}