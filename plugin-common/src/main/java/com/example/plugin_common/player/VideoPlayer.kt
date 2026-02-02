package com.example.plugin_common.player

import android.app.Dialog
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Player.Listener
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun VideoPlayer(url: String) {
    var isFullscreen by rememberSaveable { mutableStateOf(false) }
    var isBuffering by remember { mutableStateOf(false) }
    var hasError by remember { mutableStateOf(false) }

    val player = rememberVideoPlayer(
        url,
        onBufferingChange = { isBuffering = it },
        onErrorChange = { hasError = it }
    )

    if (isFullscreen) {
        FullscreenVideoDialog(
            player,
            isBuffering,
            hasError,
            onDismiss = { isFullscreen = false }
        )
    } else {
        Video(
            player,
            isBuffering,
            hasError,
            onToggleFullscreen = { isFullscreen = true }
        )
    }
}

private fun buildListener(
    onBufferingChange: (Boolean) -> Unit,
    onErrorChange: (Boolean) -> Unit
): Listener {
    return object : Listener {
        override fun onPlaybackStateChanged(state: Int) {
            onBufferingChange(state == Player.STATE_BUFFERING)
            if (state == Player.STATE_READY) {
                onErrorChange(false)
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            onBufferingChange(false)
            onErrorChange(true)
        }
    }
}

@Composable
private fun rememberVideoPlayer(
    url: String,
    onBufferingChange: (Boolean) -> Unit,
    onErrorChange: (Boolean) -> Unit
): ExoPlayer {
    val context = LocalContext.current
    var lastPosition by rememberSaveable { mutableLongStateOf(0L) }

    val player = remember(url) {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ONE
            setMediaItem(MediaItem.fromUri(url))
            seekTo(lastPosition)
        }
    }

    DisposableEffect(player) {
        val listener = buildListener(onBufferingChange, onErrorChange)
        player.addListener(listener)
        player.prepare()

        onDispose {
            lastPosition = player.currentPosition
            player.removeListener(listener)
            player.release()
        }
    }

    return player
}

@Composable
private fun PlayerSurface(player: ExoPlayer, onToggleFullscreen: () -> Unit) {
    AndroidView(
        factory = { context ->
            PlayerView(context).apply {
                this.player = player
                useController = true
                setBackgroundColor(Color.Black.toArgb())
                setFullscreenButtonClickListener {
                    onToggleFullscreen()
                }
            }
        },
        update = { playerView ->
            playerView.player = player
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
private fun Overlay(isBuffering: Boolean, hasError: Boolean) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        if (hasError) {
            Text(text = "Oops! Something went wrong.", color = Color.White)
        } else if (isBuffering) {
            CircularProgressIndicator(color = Color.White)
        }
    }
}

@Composable
private fun Video(
    player: ExoPlayer,
    isBuffering: Boolean,
    hasError: Boolean,
    onToggleFullscreen: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        PlayerSurface(player, onToggleFullscreen)
        Overlay(isBuffering, hasError)
    }
}

@Composable
private fun FullscreenVideoDialog(
    player: ExoPlayer,
    isBuffering: Boolean,
    hasError: Boolean,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Video(player, isBuffering, hasError, onToggleFullscreen = onDismiss)
    }
}