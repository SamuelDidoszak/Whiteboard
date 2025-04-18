package org.samis.whiteboard.presentation.whiteboard.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.samis.whiteboard.domain.model.DrawingTool
import org.samis.whiteboard.presentation.util.DrawingToolVisibility

@Composable
fun DrawingToolBar(
    modifier: Modifier = Modifier,
    drawingToolVisibility: DrawingToolVisibility,
    onDrawingToolClick: (drawingTool: DrawingTool) -> Unit
) {
    fun isVisible(drawingTool: DrawingTool): Boolean = drawingToolVisibility.isToolVisible(drawingTool)

    Column(modifier = modifier) {
        if (isVisible(DrawingTool.PEN)) {
            FilledIconButton(onClick = { onDrawingToolClick(DrawingTool.PEN) }) {
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = "Pen",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(25.dp)
                )
            }
        }
        if (isVisible(DrawingTool.LASER_PEN))
            AddButton(DrawingTool.LASER_PEN, onDrawingToolClick)
        if (isVisible(DrawingTool.HIGHLIGHTER))
            AddButton(DrawingTool.HIGHLIGHTER, onDrawingToolClick)
        if (isVisible(DrawingTool.PEN) || isVisible(DrawingTool.LASER_PEN))
            Spacer(modifier = Modifier.height(12.dp))

        if (isVisible(DrawingTool.LINE))
            AddButton(DrawingTool.LINE, onDrawingToolClick)
        if (isVisible(DrawingTool.ARROW))
            AddButton(DrawingTool.ARROW, onDrawingToolClick)
        if (isVisible(DrawingTool.LINE) || isVisible(DrawingTool.ARROW))
            Spacer(modifier = Modifier.height(12.dp))

        if (isVisible(DrawingTool.RECTANGLE))
            AddButton(DrawingTool.RECTANGLE, onDrawingToolClick)
        if (isVisible(DrawingTool.CIRCLE))
            AddButton(DrawingTool.CIRCLE, onDrawingToolClick)
        if (isVisible(DrawingTool.TRIANGLE))
            AddButton(DrawingTool.TRIANGLE, onDrawingToolClick)
    }
}

@Composable
private fun AddButton(drawingTool: DrawingTool, onClick: (drawingTool: DrawingTool) -> Unit) {
    FilledIconButton(onClick = { onClick(drawingTool) }) {
        Icon(
            painter = painterResource(drawingTool.res),
            contentDescription = drawingTool.name.lowercase().replaceFirstChar { it.uppercaseChar() },
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(25.dp)
        )
    }
}