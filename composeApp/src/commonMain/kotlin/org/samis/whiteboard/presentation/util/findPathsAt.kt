package org.samis.whiteboard.presentation.util

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import org.samis.whiteboard.presentation.whiteboard.util.DrawnElement

expect fun findPathsAt(
    touchPoint: Offset,
    drawnElements: List<DrawnElement>,
    rejectedElements: Set<DrawnElement>,
    canvasOffset: Offset,
    canvasScale: Float,
    hitPadding: Float = 5f,
    hitStep: Float = 5f
): List<DrawnElement>

expect fun findPathsAt(
    inRectangle: Rect,
    drawnElements: List<DrawnElement>,
    rejectedElements: Set<DrawnElement>,
    canvasOffset: Offset,
    canvasScale: Float,
    hitPadding: Float = 5f,
    hitStep: Float = 5f,
    isMarquee: Boolean
): List<DrawnElement>