package org.samis.whiteboard.presentation.whiteboard.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.launch
import org.samis.whiteboard.domain.model.DrawingTool
import org.samis.whiteboard.presentation.util.DrawingToolVisibility

@Composable
fun DrawingToolDialog(
    modifier: Modifier = Modifier,
    isDrawingToolDialogVisible: Boolean,
    drawingToolVisibility: DrawingToolVisibility,
    backgroundColor: Color,
    currentDrawingTool: DrawingTool,
    onDrawingToolClick: (drawingTool: DrawingTool) -> Unit
) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    AnimatedVisibility(
        modifier = modifier,
        visible = isDrawingToolDialogVisible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(tween(100)),
        exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(500)) + fadeOut(tween(350))
    ) {
        Box(
            modifier = Modifier
                .wrapContentSize()
                .pointerInput(Unit) {
                    detectDragGestures { _, dragAmount ->
                        coroutineScope.launch {
                            scrollState.scrollBy(-dragAmount.y)
                        }
                    }
                }
        ) {
            DrawingToolBar(
                modifier = Modifier
                    .wrapContentSize()
                    .verticalScroll(scrollState),
                drawingToolVisibility = drawingToolVisibility,
                backgroundColor = backgroundColor,
                currentDrawingTool = currentDrawingTool,
                onDrawingToolClick = onDrawingToolClick
            )
        }
    }
}