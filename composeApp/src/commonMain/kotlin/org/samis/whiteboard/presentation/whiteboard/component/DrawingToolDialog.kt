package org.samis.whiteboard.presentation.whiteboard.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    AnimatedVisibility(
        modifier = modifier,
        visible = isDrawingToolDialogVisible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(tween(100)),
        exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(500)) + fadeOut(tween(350))
    ) {
        DrawingToolBar(
            modifier = modifier,
            drawingToolVisibility = drawingToolVisibility,
            backgroundColor = backgroundColor,
            currentDrawingTool = currentDrawingTool,
            onDrawingToolClick = onDrawingToolClick
        )
    }
}