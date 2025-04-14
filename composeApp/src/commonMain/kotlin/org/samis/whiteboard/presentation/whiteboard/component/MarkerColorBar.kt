package org.samis.whiteboard.presentation.whiteboard.component

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
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.unit.Dp


@Composable
fun MarkerColorBar(
    modifier: Modifier = Modifier,
    penWidth: Dp,
    penHeight: Dp,
    markerColors: List<Color>,
    selectedMarker: Int,
    onClick: (Color) -> Unit
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