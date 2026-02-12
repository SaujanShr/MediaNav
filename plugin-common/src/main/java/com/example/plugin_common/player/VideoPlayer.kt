package com.example.plugin_common.player

import android.net.Uri
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Player.Listener
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import java.io.File

data class VideoPlayer(val repeatMode: Int = Player.REPEAT_MODE_OFF) {
    @Composable
    fun Remote(thumbnailUrl: String, videoUrl: String) {
        val (player, isBuffering, hasError) = rememberVideoPlayer(
            uri = videoUrl.toUri(),
            key = videoUrl,
            repeatMode = repeatMode
        )
        Video(player, thumbnailUrl, isBuffering, hasError)
    }

    @Composable
    fun Local(thumbnailFile: File, videoFile: File) {
        val (player, isBuffering, hasError) = rememberVideoPlayer(
            uri = Uri.fromFile(videoFile),
            key = videoFile.absolutePath,
            repeatMode = repeatMode
        )
        Video(player, thumbnailFile, isBuffering, hasError)
    }
}

@Composable
private fun Video(
    player: ExoPlayer,
    thumbnail: Any,
    isBuffering: Boolean,
    hasError: Boolean
) {
    var isFullscreen by rememberSaveable { mutableStateOf(false) }

    if (isFullscreen) {
        FullscreenVideoDialog(
            player,
            thumbnail,
            isBuffering,
            hasError,
            onDismiss = { isFullscreen = false }
        )
    } else {
        Video(
            player,
            thumbnail,
            isBuffering,
            hasError,
            onToggleFullscreen = { isFullscreen = true }
        )
    }
}

private data class VideoPlayerResult(
    val player: ExoPlayer,
    val isBuffering: Boolean,
    val hasError: Boolean
)

@Composable
private fun rememberVideoPlayer(
    uri: Uri,
    key: String,
    repeatMode: Int
): VideoPlayerResult {
    val context = LocalContext.current
    var lastPosition by rememberSaveable(key) { mutableLongStateOf(0L) }

    var isBuffering by remember { mutableStateOf(false) }
    var hasError by remember { mutableStateOf(false) }

    val player = remember(key) {
        ExoPlayer.Builder(context).build().apply {
            this.repeatMode = repeatMode
            setMediaItem(MediaItem.fromUri(uri))
            seekTo(lastPosition)
        }
    }

    DisposableEffect(player) {
        val listener = object : Listener {
            override fun onPlaybackStateChanged(state: Int) {
                isBuffering = state == Player.STATE_BUFFERING
                if (state == Player.STATE_READY) {
                    hasError = false
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                isBuffering = false
                hasError = true
            }
        }
        player.addListener(listener)
        player.prepare()

        onDispose {
            lastPosition = player.currentPosition
            player.removeListener(listener)
            player.release()
        }
    }

    return VideoPlayerResult(player, isBuffering, hasError)
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
private fun Overlay(thumbnail: Any, isBuffering: Boolean, hasError: Boolean) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        if (isBuffering && !hasError) {
            AsyncImage(
                model = thumbnail,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
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
    thumbnail: Any,
    isBuffering: Boolean,
    hasError: Boolean,
    onToggleFullscreen: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        PlayerSurface(player, onToggleFullscreen)
        Overlay(thumbnail, isBuffering, hasError)
    }
}

@Composable
private fun FullscreenVideoDialog(
    player: ExoPlayer,
    thumbnail: Any,
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
        Video(player, thumbnail, isBuffering, hasError, onToggleFullscreen = onDismiss)
    }
}
