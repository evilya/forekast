package ui.weather

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

enum class DragAnchors {
    Start, Center, End
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DraggableItem(
    modifier: Modifier = Modifier,
    state: AnchoredDraggableState<DragAnchors>,
    content: @Composable BoxScope.() -> Unit,
    startAction: @Composable BoxScope.() -> Unit = {},
    endAction: @Composable BoxScope.() -> Unit = {},
) {
    Box(modifier = modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        startAction()
        endAction()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset {
                    IntOffset(
                        x = -state
                            .requireOffset()
                            .roundToInt(),
                        y = 0
                    )
                }
                .anchoredDraggable(state, Orientation.Horizontal, reverseDirection = true),
            content = content
        )
    }
}


@Composable
fun DragToDelete(
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    onValueChanged: (DragAnchors) -> Boolean,
    content: @Composable BoxScope.() -> Unit,
) {
    val density = LocalDensity.current
    val endOffset = with(density) { 100.dp.toPx() }
    val velocityThreshold = with(density) { 80.dp.toPx() }
    val state = remember {
        AnchoredDraggableState(
            initialValue = DragAnchors.Center,
            anchors = DraggableAnchors {
                DragAnchors.Start at 0f
                DragAnchors.Center at 0f
                DragAnchors.End at endOffset
            },
            positionalThreshold = { distance -> distance * 0.75f },
            velocityThreshold = { velocityThreshold },
            animationSpec = spring(),
            confirmValueChange = onValueChanged
        )
    }
    val dragProgress = state.requireOffset() / endOffset

    val iconSize by animateDpAsState(
        targetValue = if (state.targetValue == DragAnchors.Center) 20.dp else 40.dp,
        animationSpec = tween()
    )

    DraggableItem(
        modifier = modifier,
        state = state,
        content = content,
        endAction = {
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .fillMaxHeight()
                    .align(Alignment.CenterEnd)
                    .clip(shape)
                    .background(Color.Red.copy(alpha = dragProgress))
            ) {
                Icon(
                    modifier = Modifier
                        .size(iconSize)
                        .align(Alignment.Center),
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete location",
                    tint = Color.White
                )
            }
        }
    )
}