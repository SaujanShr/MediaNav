package com.example.plugin_common.plugin

interface SecretPlugin {
    val secretKey: List<String>
    fun setSecrets(secrets: Map<String, String>)
}