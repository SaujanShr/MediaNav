package com.example.medianav.ui.shared

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext

@Composable
fun LockScreenOrientation(orientation: Int = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
    val context = LocalContext.current

    SideEffect {
        val activity = context as? Activity
        activity?.requestedOrientation = orientation
    }
}

