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
import whiteboard.composeapp.generated.resources.Res
import whiteboard.composeapp.generated.resources.ic_laser_pointer

@Composable
fun DrawingToolBar(
    modifier: Modifier = Modifier,
    onDrawingToolClick: (drawingTool: DrawingTool) -> Unit
) {
    Column(modifier = modifier) {
        FilledIconButton(onClick = { onDrawingToolClick(DrawingTool.PEN) }) {
            Icon(
                imageVector = Icons.Rounded.Edit,
                contentDescription = "Pen",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(25.dp)
            )
        }
        FilledIconButton(onClick = { onDrawingToolClick(DrawingTool.LASER_PEN) }) {
            Icon(
//                imageVector = Res.drawable.ic_laser_pointer,
                painter = painterResource(DrawingTool.LASER_PEN.res),
                contentDescription = "Laser Pointer",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(25.dp)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        FilledIconButton(onClick = { onDrawingToolClick(DrawingTool.LINE) }) {
            Icon(
                painter = painterResource(DrawingTool.LINE.res),
                contentDescription = "Line",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(25.dp)
            )
        }
        FilledIconButton(onClick = { onDrawingToolClick(DrawingTool.ARROW) }) {
            Icon(
                painter = painterResource(DrawingTool.ARROW.res),
                contentDescription = "Arrow",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(25.dp)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        FilledIconButton(onClick = { onDrawingToolClick(DrawingTool.RECTANGLE) }) {
            Icon(
                painter = painterResource(DrawingTool.RECTANGLE.res),
                contentDescription = "Rectangle",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(25.dp)
            )
        }
        FilledIconButton(onClick = { onDrawingToolClick(DrawingTool.CIRCLE) }) {
            Icon(
                painter = painterResource(DrawingTool.CIRCLE.res),
                contentDescription = "Circle",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(25.dp)
            )
        }
        FilledIconButton(onClick = { onDrawingToolClick(DrawingTool.TRIANGLE) }) {
            Icon(
                painter = painterResource(DrawingTool.TRIANGLE.res),
                contentDescription = "Triangle",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(25.dp)
            )
        }
    }
}