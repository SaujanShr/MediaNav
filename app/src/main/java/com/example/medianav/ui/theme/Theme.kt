package com.example.medianav.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun MediaNavTheme(
    theme: Theme = Theme.DEFAULT,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val darkTheme = when (theme) {
        Theme.DEFAULT -> isSystemInDarkTheme()
        Theme.LIGHT -> false
        Theme.DARK -> true
    }
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme(
            primary = DarkColors.Primary,
            onPrimary = DarkColors.OnPrimary,
            primaryContainer = DarkColors.PrimaryContainer,
            onPrimaryContainer = DarkColors.OnPrimaryContainer,
            secondary = DarkColors.Secondary,
            onSecondary = DarkColors.OnSecondary,
            secondaryContainer = DarkColors.SecondaryContainer,
            onSecondaryContainer = DarkColors.OnSecondaryContainer,
            tertiary = DarkColors.Tertiary,
            onTertiary = DarkColors.OnTertiary,
            tertiaryContainer = DarkColors.TertiaryContainer,
            onTertiaryContainer = DarkColors.OnTertiaryContainer,
            background = DarkColors.Background,
            onBackground = DarkColors.OnBackground,
            surface = DarkColors.Surface,
            onSurface = DarkColors.OnSurface,
            surfaceVariant = DarkColors.SurfaceVariant,
            onSurfaceVariant = DarkColors.OnSurfaceVariant,
            outline = DarkColors.Outline,
            error = DarkColors.Error,
            onError = DarkColors.OnError,
            errorContainer = DarkColors.ErrorContainer,
            onErrorContainer = DarkColors.OnErrorContainer
        )
        else -> lightColorScheme(
            primary = LightColors.Primary,
            onPrimary = LightColors.OnPrimary,
            primaryContainer = LightColors.PrimaryContainer,
            onPrimaryContainer = LightColors.OnPrimaryContainer,
            secondary = LightColors.Secondary,
            onSecondary = LightColors.OnSecondary,
            secondaryContainer = LightColors.SecondaryContainer,
            onSecondaryContainer = LightColors.OnSecondaryContainer,
            tertiary = LightColors.Tertiary,
            onTertiary = LightColors.OnTertiary,
            tertiaryContainer = LightColors.TertiaryContainer,
            onTertiaryContainer = LightColors.OnTertiaryContainer,
            background = LightColors.Background,
            onBackground = LightColors.OnBackground,
            surface = LightColors.Surface,
            onSurface = LightColors.OnSurface,
            surfaceVariant = LightColors.SurfaceVariant,
            onSurfaceVariant = LightColors.OnSurfaceVariant,
            outline = LightColors.Outline,
            error = LightColors.Error,
            onError = LightColors.OnError,
            errorContainer = LightColors.ErrorContainer,
            onErrorContainer = LightColors.OnErrorContainer
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

enum class Theme(val value: String) {
    DEFAULT("default"),
    LIGHT("light"),
    DARK("dark");

    companion object {
        private val map = entries.associateBy(Theme::value)

        fun fromValue(value: String?): Theme =
            map[value] ?: DEFAULT
    }
}