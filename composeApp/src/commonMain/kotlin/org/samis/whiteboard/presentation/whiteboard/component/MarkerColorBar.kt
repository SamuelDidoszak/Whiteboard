package org.samis.whiteboard.presentation.whiteboard.component

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.unit.Dp
import org.samis.whiteboard.domain.model.DrawingTool
import org.samis.whiteboard.presentation.theme.eraserHandle
import org.samis.whiteboard.presentation.theme.eraserHard
import org.samis.whiteboard.presentation.theme.eraserSoft


@Composable
fun MarkerColorBar(
    modifier: Modifier = Modifier,
    penWidth: Dp,
    penHeight: Dp,
    markerColors: List<Color>,
    selectedMarker: Int,
    selectedDrawingTool: DrawingTool,
    onClick: (Color) -> Unit,
    onEraserClick: (DrawingTool) -> Unit
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom
    ) {
        for (i in 0 until 4) {
            val open = i == selectedMarker
            Marker(markerColors[i], open, penWidth, penHeight, onClick)
            Spacer(Modifier.width(10.dp))
        }
        Eraser(penWidth, penHeight, selectedDrawingTool, onEraserClick)
    }
}

@Composable
private fun Marker(color: Color, open: Boolean, width: Dp, height: Dp, onClick: (Color) -> Unit) {
    Box(modifier = Modifier.clickable { onClick.invoke(color) }) {
        if (open)
            OpenMarker(color, width, height)
        else
            ClosedMarker(color, width, height)
    }
}


@Composable
private fun ClosedMarker(color: Color, width: Dp, height: Dp) {
    Column {
        Box(
            modifier = Modifier
                .width(width)
                .height(height * 5 / 6)
                .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                .background(color)
        )
        Box(
            modifier = Modifier
                .width(width)
                .height(2.dp)
                .background(Color.White)
        )
        Box(
            modifier = Modifier
                .width(width)
                .height(height / 6)
                .background(color)
        )
    }
}

@Composable
private fun OpenMarker(color: Color, width: Dp, height: Dp) {
    Canvas(modifier = Modifier.width(width).height(height)) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Bottom Rectangle
        drawRect(
            color = color,
            topLeft = Offset(0f, canvasHeight * 5 / 6),
            size = Size(canvasWidth, canvasHeight / 6),
            style = Fill
        )

        // Middle Section
        drawRect(
            color = color,
            topLeft = Offset(canvasWidth * 0.1f, canvasHeight * 0.65f),
            size = Size(canvasWidth * 0.8f, canvasHeight * 0.3f),
            style = Fill
        )

        val middlePath = Path().apply {
            moveTo(canvasWidth * 0.2f, canvasHeight * 0.8f)
            cubicTo(
                canvasWidth * 0.1f, canvasHeight * 0.7f,
                canvasWidth * 0.15f, canvasHeight * 0.5f,
                canvasWidth * 0.3f, canvasHeight * 0.3f
            )
            lineTo(canvasWidth * 0.7f, canvasHeight * 0.3f)
            cubicTo(
                canvasWidth * 0.85f, canvasHeight * 0.5f,
                canvasWidth * 0.9f, canvasHeight * 0.7f,
                canvasWidth * 0.8f, canvasHeight * 0.8f
            )
            close()
        }
        drawPath(path = middlePath, color = color, style = Fill)

        // Tip
        val path = curvedTip(canvasWidth * 0.4f, canvasHeight * 0.28f, canvasWidth * 0.2f, canvasHeight * 0.16f)
        drawPath(path = path, color = color, style = Fill)

        drawLine(
            color = Color.White,
            start = Offset(0f, canvasHeight * 5 / 6),
            strokeWidth = 2f,
            end = Offset(canvasWidth, canvasHeight * 5 / 6)
        )
        drawLine(
            color = Color.White,
            start = Offset(canvasWidth * 0.4f, canvasHeight * 0.3f),
            strokeWidth = 2f,
            end = Offset(canvasWidth * 0.6f, canvasHeight * 0.28f)
        )
    }
}

private fun curvedTip(
    startX: Float,
    startY: Float,
    width: Float,
    height: Float,
): Path {
    return Path().apply {
        moveTo(startX, startY)
        cubicTo(
            startX, startY - height / 2f,
            startX + width / 4f, startY - height,
            startX + width / 2f, startY - height
        )
        cubicTo(
            startX + width * 3 / 4f, startY - height,
            startX + width, startY - height / 2f,
            startX + width, startY
        )
        close()
    }
}

@Composable
fun Eraser(width: Dp, height: Dp, selectedDrawingTool: DrawingTool, onEraserClick: (DrawingTool) -> Unit) {
    val isOffsetDown = selectedDrawingTool == DrawingTool.ERASER || selectedDrawingTool == DrawingTool.DELETER
    val offsetDownValue = height / 5

    val yOffset: Dp by animateDpAsState(
        targetValue = if (isOffsetDown) offsetDownValue else 0.dp,
        animationSpec = tween(durationMillis = 300)
    )
    Column(
        modifier = Modifier
            .width(width)
            .height(height)
            .offset(y = yOffset)
            .clickable {
                val eraserType =
                    if (selectedDrawingTool == DrawingTool.ERASER) DrawingTool.DELETER
                    else DrawingTool.ERASER
                onEraserClick(eraserType)
            }
    ) {
        val eraserColor =
            if (selectedDrawingTool == DrawingTool.DELETER) eraserHard else eraserSoft
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(height / 4)
                .background(eraserColor, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .align(Alignment.CenterHorizontally)
        )
        Box(
            modifier = Modifier
                .width(width)
                .height(2.dp)
                .background(Color.White)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height * 3 / 4)
                .background(eraserHandle)
        )
    }
}