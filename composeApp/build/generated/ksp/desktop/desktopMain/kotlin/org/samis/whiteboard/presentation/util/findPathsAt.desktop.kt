package org.samis.whiteboard.presentation.util

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import org.samis.whiteboard.domain.model.DrawnPath
import org.samis.whiteboard.presentation.whiteboard.util.DrawnElement

actual fun findPathsAt(
    touchPoint: Offset,
    drawnElements: List<DrawnElement>,
    rejectedElements: Set<DrawnElement>,
    canvasOffset: Offset,
    canvasScale: Float,
    hitPadding: Float,
    hitStep: Float
): List<DrawnElement> {
    TODO("Not yet implemented")
}

actual fun findPathsAt(
    inRectangle: Rect,
    drawnElements: List<DrawnElement>,
    rejectedElements: Set<DrawnElement>,
    canvasOffset: Offset,
    canvasScale: Float,
    hitPadding: Float,
    hitStep: Float,
    isMarquee: Boolean
): List<DrawnElement> {
    TODO("Not yet implemented")
}