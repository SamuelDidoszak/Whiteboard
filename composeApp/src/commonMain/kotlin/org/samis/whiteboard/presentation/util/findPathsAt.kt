package org.samis.whiteboard.presentation.util

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import org.samis.whiteboard.domain.model.DrawnPath

expect fun findPathsAt(
    touchPoint: Offset,
    drawnPaths: List<DrawnPath>,
    rejectedPaths: Set<DrawnPath>,
    canvasOffset: Offset,
    canvasScale: Float,
    hitPadding: Float = 5f,
    hitStep: Float = 5f
): List<DrawnPath>

expect fun findPathsAt(
    inRectangle: Rect,
    drawnPaths: List<DrawnPath>,
    rejectedPaths: Set<DrawnPath>,
    canvasOffset: Offset,
    canvasScale: Float,
    hitPadding: Float = 5f,
    hitStep: Float = 5f,
    isMarquee: Boolean
): List<DrawnPath>