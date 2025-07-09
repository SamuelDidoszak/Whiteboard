package org.samis.whiteboard.presentation.whiteboard.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min

private val MAX_STROKE_WIDTH = 15f

@Composable
fun StrokeWidthBar(
    modifier: Modifier = Modifier,
    minButtonSize: Dp,
    maxButtonSize: Dp,
    strokeWidthList: List<Float>,
    activeButton: Int,
    canvasColor: Color,
    onClick: (Int) -> Unit
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until 3) {
            val percentage = strokeWidthList[i] / MAX_STROKE_WIDTH
            Box(
                modifier = Modifier
                    .size(maxButtonSize)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                StrokeWidthButton(
                    size = ((maxButtonSize - minButtonSize) * min(percentage, 1f) + minButtonSize),
                    percentage = percentage,
                    color = getButtonContentColor(canvasColor),
                    canvasColor = canvasColor,
                    filled = i == activeButton,
                    i,
                    onClick
                )
            }
            Spacer(Modifier.width(2.dp))
        }
    }
}

@Composable
private fun StrokeWidthButton(
    size: Dp,
    percentage: Float,
    color: Color,
    canvasColor: Color,
    filled: Boolean,
    buttonId: Int,
    onClick: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .shadow(if (filled) 4.dp else 5.5.dp, CircleShape, clip = false)
            .clip(CircleShape)
            .background(canvasColor)
            .border(3.dp, color, CircleShape)
            .clickable { onClick(buttonId) }
    ) {
        if (filled) {
            Box(
                modifier = Modifier
                    .size((size / 2) * max(percentage, 1f))
                    .clip(CircleShape)
                    .background(color)
                    .align(Alignment.Center)
            )
        }
    }
}

private fun getButtonContentColor(backgroundColor: Color): Color {
    val isBackgroundLight = backgroundColor.luminance() > 0.5
    return if (isBackgroundLight) Color.Black else Color.White
}