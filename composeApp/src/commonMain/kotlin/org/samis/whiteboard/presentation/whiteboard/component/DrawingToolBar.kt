package org.samis.whiteboard.presentation.whiteboard.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.samis.whiteboard.domain.model.DrawingTool
import org.samis.whiteboard.presentation.util.DrawingToolVisibility

@Composable
fun DrawingToolBar(
    modifier: Modifier = Modifier,
    drawingToolVisibility: DrawingToolVisibility,
    backgroundColor: Color,
    currentDrawingTool: DrawingTool,
    onDrawingToolClick: (drawingTool: DrawingTool) -> Unit
) {
    fun isVisible(drawingTool: DrawingTool): Boolean = drawingToolVisibility.isToolVisible(drawingTool)
    fun isTool(drawingTool: DrawingTool): Boolean = currentDrawingTool == drawingTool

    Column(modifier = modifier) {
        if (isVisible(DrawingTool.PEN)) {
            ElevatedIconButton(backgroundColor = backgroundColor, isSelected = isTool(DrawingTool.PEN), onClick = { onDrawingToolClick(DrawingTool.PEN) }) {
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = "Pen",
                    modifier = Modifier.size(25.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
        }
        if (isVisible(DrawingTool.LASER_PEN))
            AddButton(DrawingTool.LASER_PEN, backgroundColor, isTool(DrawingTool.LASER_PEN), onDrawingToolClick)
        if (isVisible(DrawingTool.HIGHLIGHTER))
            AddButton(DrawingTool.HIGHLIGHTER, backgroundColor, isTool(DrawingTool.HIGHLIGHTER), onDrawingToolClick)
        if (isVisible(DrawingTool.PEN) || isVisible(DrawingTool.LASER_PEN))
            Spacer(modifier = Modifier.height(12.dp))

        if (isVisible(DrawingTool.LINE))
            AddButton(DrawingTool.LINE, backgroundColor, isTool(DrawingTool.LINE), onDrawingToolClick)
        if (isVisible(DrawingTool.ARROW))
            AddButton(DrawingTool.ARROW, backgroundColor, isTool(DrawingTool.ARROW), onDrawingToolClick)
        if (isVisible(DrawingTool.LINE) || isVisible(DrawingTool.ARROW))
            Spacer(modifier = Modifier.height(12.dp))

        if (isVisible(DrawingTool.RECTANGLE))
            AddButton(DrawingTool.RECTANGLE, backgroundColor, isTool(DrawingTool.RECTANGLE), onDrawingToolClick)
        if (isVisible(DrawingTool.CIRCLE))
            AddButton(DrawingTool.CIRCLE, backgroundColor, isTool(DrawingTool.CIRCLE), onDrawingToolClick)
        if (isVisible(DrawingTool.TRIANGLE))
            AddButton(DrawingTool.TRIANGLE, backgroundColor, isTool(DrawingTool.TRIANGLE), onDrawingToolClick)
    }
}

@Composable
private fun AddButton(drawingTool: DrawingTool, backgroundColor: Color, isSelected: Boolean, onClick: (drawingTool: DrawingTool) -> Unit, addSpacer: Boolean = true) {
    ElevatedIconButton(backgroundColor = backgroundColor, isSelected = isSelected, onClick = { onClick(drawingTool) }) {
        Icon(
            painter = painterResource(drawingTool.res),
            contentDescription = drawingTool.name.lowercase().replaceFirstChar { it.uppercaseChar() },
            modifier = Modifier.size(25.dp)
        )
    }
    if (addSpacer)
        Spacer(modifier = Modifier.height(6.dp))
}