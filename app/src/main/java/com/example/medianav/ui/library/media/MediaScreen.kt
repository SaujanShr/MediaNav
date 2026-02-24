package com.example.medianav.ui.library.media

import android.content.pm.ActivityInfo
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.example.medianav.ui.library.LibraryViewModel
import com.example.medianav.ui.shared.LockScreenOrientation
import com.example.plugin_common.library.LibraryItem
import com.example.plugin_common.library.LibraryItemStatus
import com.example.plugin_common.plugin.MediaPlugin
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaScreen(
    viewModel: LibraryViewModel,
    onBack: () -> Unit
) {
    LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)

    val currentItem by viewModel.currentItem.collectAsState()
    val plugin by viewModel.currentPlugin.collectAsState()
    val canNavigateNext by viewModel.canNavigateNext.collectAsState()
    val canNavigatePrevious by viewModel.canNavigatePrevious.collectAsState()

    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val screenWidthPx = with(density) { 400.dp.toPx() } // approximate screen width

    var dragOffset by remember { mutableFloatStateOf(0f) }
    val offsetX = remember { Animatable(0f) }
    val swipeThreshold = 100f

    // Reset animation when item changes
    LaunchedEffect(currentItem?.id) {
        offsetX.snapTo(0f)
        dragOffset = 0f
    }

    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
    ) {
        TopAppBar(
            title = { },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        )

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
                            scope.launch {
                                when {
                                    dragOffset > swipeThreshold && canNavigatePrevious -> {
                                        // Animate out to the right
                                        offsetX.animateTo(
                                            targetValue = screenWidthPx,
                                            animationSpec = tween(durationMillis = 200)
                                        )
                                        viewModel.navigatePrevious()
                                        // Slide in from left
                                        offsetX.snapTo(-screenWidthPx)
                                        offsetX.animateTo(
                                            targetValue = 0f,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessLow
                                            )
                                        )
                                    }
                                    dragOffset < -swipeThreshold && canNavigateNext -> {
                                        // Animate out to the left
                                        offsetX.animateTo(
                                            targetValue = -screenWidthPx,
                                            animationSpec = tween(durationMillis = 200)
                                        )
                                        viewModel.navigateNext()
                                        // Slide in from right
                                        offsetX.snapTo(screenWidthPx)
                                        offsetX.animateTo(
                                            targetValue = 0f,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessLow
                                            )
                                        )
                                    }
                                    else -> {
                                        // Snap back to center
                                        offsetX.animateTo(
                                            targetValue = 0f,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessMedium
                                            )
                                        )
                                    }
                                }
                                dragOffset = 0f
                            }
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            dragOffset += dragAmount
                            scope.launch {
                                // Update offset with resistance at edges
                                val resistance = 0.5f
                                val newOffset = offsetX.value + dragAmount * resistance

                                // Apply resistance when trying to swipe beyond limits
                                val shouldApply = when {
                                    newOffset > 0 && canNavigatePrevious -> true
                                    newOffset < 0 && canNavigateNext -> true
                                    newOffset.absoluteValue < 50 -> true // Allow small movement
                                    else -> false
                                }

                                if (shouldApply) {
                                    offsetX.snapTo(newOffset)
                                }
                            }
                        }
                    )
                }
        ) {
            MediaDetailContent(
                item = currentItem,
                plugin = plugin,
                viewModel = viewModel,
                offsetX = offsetX
            )
        }
    }
}

@Composable
private fun MediaDetailContent(
    item: LibraryItem?,
    plugin: MediaPlugin?,
    viewModel: LibraryViewModel,
    offsetX: Animatable<Float, AnimationVector1D>
) {
    val offset = offsetX.value
    val alpha = 1f - (offset.absoluteValue / 1000f).coerceIn(0f, 0.3f)

    item?.let { currentItem ->
        plugin?.let { p ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .offset { IntOffset(offset.roundToInt(), 0) }
                    .graphicsLayer {
                        this.alpha = alpha
                        // Add slight scale effect
                        val scale = 1f - (offset.absoluteValue / 5000f).coerceIn(0f, 0.05f)
                        scaleX = scale
                        scaleY = scale
                    }
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(150.dp)
                            .aspectRatio(75f / 106f)
                    ) {
                        p.PreviewContent(currentItem)
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        p.SummaryContent(currentItem)
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    IconButton(onClick = { viewModel.toggleStatus(LibraryItemStatus.VIEWED) }) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "Viewed",
                            tint = if (currentItem.status == LibraryItemStatus.VIEWED) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                    IconButton(onClick = { viewModel.toggleStatus(LibraryItemStatus.LIKED) }) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Liked",
                            tint = if (currentItem.status == LibraryItemStatus.LIKED) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                    IconButton(onClick = { viewModel.toggleSaved() }) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = null,
                            tint = if (currentItem.saved) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }

                p.DescriptionContent(currentItem)
                p.AttributeContent(currentItem)
            }
        }
    }
}

