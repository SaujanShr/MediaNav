package com.example.medianav.ui.shared

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext

/**
 * Locks the screen orientation to portrait mode for the current composable.
 * When the composable leaves the composition, the orientation lock is removed.
 */
@Composable
fun LockScreenOrientation(orientation: Int = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
    val context = LocalContext.current
    DisposableEffect(orientation) {
        val activity = context as? Activity ?: return@DisposableEffect onDispose {}
        activity.requestedOrientation = orientation
        onDispose {
            // Restore to unspecified to allow rotation
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }
}


