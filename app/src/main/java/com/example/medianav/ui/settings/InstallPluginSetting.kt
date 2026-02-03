package com.example.medianav.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
internal fun InstallPluginSetting(viewModel: SettingsViewModel) {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        viewModel.installPlugin(context, uri)
    }

    Setting(
        viewModel = viewModel,
        title = "Install plugin",
        subtitle = "Install a plugin from APK",
        leftIcon = Icons.Outlined.Extension,
        type = SettingType.INSTALL,
        onClick = { launcher.launch("application/vnd.android.package-archive") }
    )
}
