package com.example.medianav.ui.animation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun AnimatedDismiss(
    visible: Boolean,
    onDismiss: () -> Unit,
    animationDuration: Int = 250,
    content: @Composable (dismiss: suspend () -> Unit) -> Unit
) {
    if (!visible) return

    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }

    val screenWidthPx = with(LocalDensity.current) { 400.dp.toPx() }

    val animatedDismiss = createAnimatedDismiss(
        offsetX = offsetX,
        screenWidthPx = screenWidthPx,
        animationDuration = animationDuration,
        onDismiss = onDismiss
    )

    HandleBackPress(
        scope = scope,
        animatedDismiss = animatedDismiss
    )

    AnimatedContent(
        offsetX = offsetX,
        screenWidthPx = screenWidthPx,
        animatedDismiss = animatedDismiss,
        content = content
    )
}

@Composable
private fun createAnimatedDismiss(
    offsetX: Animatable<Float, AnimationVector1D>,
    screenWidthPx: Float,
    animationDuration: Int,
    onDismiss: () -> Unit
): suspend () -> Unit {
    return remember(offsetX, screenWidthPx, animationDuration, onDismiss) {
        {
            offsetX.animateTo(
                targetValue = screenWidthPx,
                animationSpec = tween(durationMillis = animationDuration)
            )
            onDismiss()
            offsetX.snapTo(0f)
        }
    }
}

@Composable
private fun HandleBackPress(
    scope: CoroutineScope,
    animatedDismiss: suspend () -> Unit
) {
    BackHandler(enabled = true) {
        scope.launch {
            animatedDismiss()
        }
    }
}

@Composable
private fun AnimatedContent(
    offsetX: Animatable<Float, AnimationVector1D>,
    screenWidthPx: Float,
    animatedDismiss: suspend () -> Unit,
    content: @Composable (dismiss: suspend () -> Unit) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .offset { IntOffset(offsetX.value.roundToInt(), 0) }
            .graphicsLayer {
                alpha = calculateAlpha(offsetX.value, screenWidthPx)
            }
    ) {
        content(animatedDismiss)
    }
}

private fun calculateAlpha(offsetValue: Float, screenWidthPx: Float): Float {
    return 1f - (offsetValue / screenWidthPx * 0.3f).coerceIn(0f, 0.3f)
}
