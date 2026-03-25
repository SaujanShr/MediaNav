package com.example.medianav.ui.animation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

@Composable
fun SwipeableContent(
    contentId: Any?,
    canNavigateNext: Boolean,
    canNavigatePrevious: Boolean,
    onNavigateNext: () -> Unit,
    onNavigatePrevious: () -> Unit,
    content: @Composable (offset: Float, alpha: Float) -> Unit
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val screenWidthPx = with(density) { 400.dp.toPx() }

    var dragOffset by remember { mutableFloatStateOf(0f) }
    val offsetX = remember { Animatable(0f) }
    val swipeThreshold = 100f

    LaunchedEffect(contentId) {
        offsetX.snapTo(0f)
        dragOffset = 0f
    }

    val offset = offsetX.value
    val alpha = calculateAlpha(offset)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(canNavigateNext, canNavigatePrevious) {
                detectHorizontalDragGestures(
                    onDragStart = {
                        scope.launch {
                            offsetX.stop()
                        }
                    },
                    onDragEnd = {
                        handleDragEnd(
                            scope = scope,
                            dragOffset = dragOffset,
                            swipeThreshold = swipeThreshold,
                            canNavigatePrevious = canNavigatePrevious,
                            canNavigateNext = canNavigateNext,
                            offsetX = offsetX,
                            screenWidthPx = screenWidthPx,
                            onNavigatePrevious = onNavigatePrevious,
                            onNavigateNext = onNavigateNext,
                            onDragOffsetReset = { dragOffset = 0f }
                        )
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        handleDrag(
                            scope = scope,
                            dragAmount = dragAmount,
                            dragOffset = dragOffset,
                            offsetX = offsetX,
                            canNavigatePrevious = canNavigatePrevious,
                            canNavigateNext = canNavigateNext,
                            onDragOffsetUpdate = { dragOffset = it }
                        )
                    }
                )
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(offset.roundToInt(), 0) }
                .graphicsLayer {
                    this.alpha = alpha
                    val scale = calculateScale(offset)
                    scaleX = scale
                    scaleY = scale
                }
        ) {
            content(offset, alpha)
        }
    }
}

private fun calculateAlpha(offset: Float): Float {
    return 1f - (offset.absoluteValue / 1000f).coerceIn(0f, 0.3f)
}

private fun calculateScale(offset: Float): Float {
    return 1f - (offset.absoluteValue / 5000f).coerceIn(0f, 0.05f)
}

private fun handleDragEnd(
    scope: kotlinx.coroutines.CoroutineScope,
    dragOffset: Float,
    swipeThreshold: Float,
    canNavigatePrevious: Boolean,
    canNavigateNext: Boolean,
    offsetX: Animatable<Float, *>,
    screenWidthPx: Float,
    onNavigatePrevious: () -> Unit,
    onNavigateNext: () -> Unit,
    onDragOffsetReset: () -> Unit
) {
    scope.launch {
        when {
            dragOffset > swipeThreshold && canNavigatePrevious -> {
                animateNavigatePrevious(offsetX, screenWidthPx, onNavigatePrevious)
            }
            dragOffset < -swipeThreshold && canNavigateNext -> {
                animateNavigateNext(offsetX, screenWidthPx, onNavigateNext)
            }
            else -> {
                animateSnapBack(offsetX)
            }
        }
        onDragOffsetReset()
    }
}

private suspend fun animateNavigatePrevious(
    offsetX: Animatable<Float, *>,
    screenWidthPx: Float,
    onNavigatePrevious: () -> Unit
) {
    offsetX.animateTo(
        targetValue = screenWidthPx,
        animationSpec = tween(durationMillis = 200)
    )
    onNavigatePrevious()
    offsetX.snapTo(-screenWidthPx)
    offsetX.animateTo(
        targetValue = 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
}

private suspend fun animateNavigateNext(
    offsetX: Animatable<Float, *>,
    screenWidthPx: Float,
    onNavigateNext: () -> Unit
) {
    offsetX.animateTo(
        targetValue = -screenWidthPx,
        animationSpec = tween(durationMillis = 200)
    )
    onNavigateNext()
    offsetX.snapTo(screenWidthPx)
    offsetX.animateTo(
        targetValue = 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
}

private suspend fun animateSnapBack(offsetX: Animatable<Float, *>) {
    offsetX.animateTo(
        targetValue = 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )
}

private fun handleDrag(
    scope: kotlinx.coroutines.CoroutineScope,
    dragAmount: Float,
    dragOffset: Float,
    offsetX: Animatable<Float, *>,
    canNavigatePrevious: Boolean,
    canNavigateNext: Boolean,
    onDragOffsetUpdate: (Float) -> Unit
) {
    onDragOffsetUpdate(dragOffset + dragAmount)
    scope.launch {
        val resistance = 0.5f
        val newOffset = offsetX.value + dragAmount * resistance

        val shouldApply = when {
            newOffset > 0 && canNavigatePrevious -> true
            newOffset < 0 && canNavigateNext -> true
            newOffset.absoluteValue < 50 -> true
            else -> false
        }

        if (shouldApply) {
            offsetX.snapTo(newOffset)
        }
    }
}

