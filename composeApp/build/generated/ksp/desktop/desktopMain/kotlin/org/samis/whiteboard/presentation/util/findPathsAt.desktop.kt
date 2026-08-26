package org.samis.whiteboard.presentation.util

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import org.samis.whiteboard.domain.model.DrawnPath

actual fun findPathsAt(
    touchPoint: Offset,
    drawnPaths: List<DrawnPath>,
    rejectedPaths: Set<DrawnPath>,
    canvasOffset: Offset,
    canvasScale: Float,
    hitPadding: Float,
    hitStep: Float
): List<DrawnPath> {
    TODO("Not yet implemented")
}

actual fun findPathsAt(
    inRectangle: Rect,
    drawnPaths: List<DrawnPath>,
    rejectedPaths: Set<DrawnPath>,
    canvasOffset: Offset,
    canvasScale: Float,
    hitPadding: Float,
    hitStep: Float,
    isMarquee: Boolean
): List<DrawnPath> {
    TODO("Not yet implemented")
}