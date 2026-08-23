package org.samis.whiteboard.presentation.whiteboard.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.samis.whiteboard.domain.model.DrawingTool
import org.samis.whiteboard.presentation.util.equalsDelta
import whiteboard.composeapp.generated.resources.Res
import whiteboard.composeapp.generated.resources.ZoomInLens
import whiteboard.composeapp.generated.resources.ZoomLens
import whiteboard.composeapp.generated.resources.ZoomOutLens
import whiteboard.composeapp.generated.resources.ic_redo
import whiteboard.composeapp.generated.resources.ic_undo

@Composable
fun CommandBarHorizontal(
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    canvasScale: Float,
    selectedDrawingTool: DrawingTool,
    isZoomSliderOpen: Boolean,
    onHomeIconClick: () -> Unit,
    onMenuIconClick: () -> Unit,
    onSaveIconClick: () -> Unit,
    onUndoIconClick: () -> Unit,
    onRedoIconClick: () -> Unit,
    onZoomButtonClick: () -> Unit,
    onZoomButtonDoubleClick: () -> Unit
) {
    Row(modifier = modifier) {
        ElevatedIconButton(backgroundColor = backgroundColor, isSelected = false, onClick = onHomeIconClick) {
            Icon(
                imageVector = Icons.Filled.Home,
                contentDescription = "Home",
                modifier = Modifier.size(25.dp)
            )
        }
        Spacer(modifier = Modifier.size(6.dp))
        ElevatedIconButton(backgroundColor = backgroundColor, isSelected = false, onClick = onMenuIconClick) {
            Icon(
                imageVector = Icons.Filled.Menu,
                contentDescription = "Command Palette",
                modifier = Modifier.size(25.dp)
            )
        }
        Spacer(modifier = Modifier.size(6.dp))
        if (selectedDrawingTool == DrawingTool.CANVAS_PANNER || !canvasScale.equalsDelta(1f) || isZoomSliderOpen) {
            ElevatedIconButton(backgroundColor = backgroundColor, isSelected = false, onClick = onZoomButtonClick, onDoubleClick = onZoomButtonDoubleClick) {
                Icon(
                    painter = painterResource(getDrawable(canvasScale)),
                    contentDescription = "Home",
                    modifier = Modifier.size(25.dp)
                )
            }
            Spacer(modifier = Modifier.size(6.dp))
        }
        ElevatedIconButton(backgroundColor = backgroundColor, isSelected = false, onClick = onSaveIconClick) {
            Icon(
                imageVector = Icons.Filled.Download,
                contentDescription = "Save As Picture",
                modifier = Modifier.size(25.dp)
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        ElevatedIconButton(backgroundColor = backgroundColor, isSelected = false, onClick = onUndoIconClick) {
            Icon(
                painter = painterResource(Res.drawable.ic_undo),
                contentDescription = "Undo",
                modifier = Modifier.size(25.dp)
            )
        }
        Spacer(modifier = Modifier.size(6.dp))
        ElevatedIconButton(backgroundColor = backgroundColor, isSelected = false, onClick = onRedoIconClick) {
            Icon(
                painter = painterResource(Res.drawable.ic_redo),
                contentDescription = "Redo",
                modifier = Modifier.size(25.dp)
            )
        }
    }
}

@Composable
fun CommandBarVertical(
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    canvasScale: Float,
    selectedDrawingTool: DrawingTool,
    isZoomSliderOpen: Boolean,
    onHomeIconClick: () -> Unit,
    onMenuIconClick: () -> Unit,
    onSaveIconClick: () -> Unit,
    onUndoIconClick: () -> Unit,
    onRedoIconClick: () -> Unit,
    onZoomButtonClick: () -> Unit,
    onZoomButtonDoubleClick: () -> Unit
) {
    Column(modifier = modifier) {
        ElevatedIconButton(backgroundColor = backgroundColor, isSelected = false, onClick = onHomeIconClick) {
            Icon(
                imageVector = Icons.Filled.Home,
                contentDescription = "Home",
                modifier = Modifier.size(25.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        ElevatedIconButton(backgroundColor = backgroundColor, isSelected = false, onClick = onMenuIconClick) {
            Icon(
                imageVector = Icons.Filled.Menu,
                contentDescription = "Command Palette",
                modifier = Modifier.size(25.dp)
            )
        }
        Spacer(modifier = Modifier.height(18.dp))
        ElevatedIconButton(backgroundColor = backgroundColor, isSelected = false, onClick = onUndoIconClick) {
            Icon(
                painter = painterResource(Res.drawable.ic_undo),
                contentDescription = "Undo",
                modifier = Modifier.size(25.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        ElevatedIconButton(backgroundColor = backgroundColor, isSelected = false, onClick = onRedoIconClick) {
            Icon(
                painter = painterResource(Res.drawable.ic_redo),
                contentDescription = "Redo",
                modifier = Modifier.size(25.dp)
            )
        }
        Spacer(modifier = Modifier.height(18.dp))
        if (selectedDrawingTool == DrawingTool.CANVAS_PANNER || !canvasScale.equalsDelta(1f) || isZoomSliderOpen) {
            ElevatedIconButton(backgroundColor = backgroundColor, isSelected = false, onClick = onZoomButtonClick, onDoubleClick = onZoomButtonDoubleClick) {
                Icon(
                    painter = painterResource(getDrawable(canvasScale)),
                    contentDescription = "Home",
                    modifier = Modifier.size(25.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
        }
        ElevatedIconButton(backgroundColor = backgroundColor, isSelected = false, onClick = onSaveIconClick) {
            Icon(
                imageVector = Icons.Filled.Download,
                contentDescription = "Save As Picture",
                modifier = Modifier.size(25.dp)
            )
        }
    }
}

private fun getDrawable(canvasScale: Float): DrawableResource {
    return if (canvasScale.equalsDelta(1f, 0.05f))
        Res.drawable.ZoomLens
    else if (canvasScale < 1f)
        Res.drawable.ZoomOutLens
    else
        Res.drawable.ZoomInLens
}