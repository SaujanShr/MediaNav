package com.example.medianav.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.plugin_common.settings.Setting

@Composable
internal fun installPluginSetting(viewModel: SettingsViewModel): Setting {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        viewModel.installPlugin(context, uri)
    }

    return Setting(
        title = "Install plugin",
        subtitle = "Install a plugin from APK",
        leftIcon = Icons.Outlined.Extension,
        onClick = { launcher.launch("application/vnd.android.package-archive") }
    )
}
