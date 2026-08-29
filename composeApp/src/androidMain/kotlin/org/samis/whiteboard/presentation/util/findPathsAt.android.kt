package org.samis.whiteboard.presentation.util

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.asAndroidPath
import org.samis.whiteboard.domain.model.DrawingTool
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
    val touchBounds = Rect(
        left = touchPoint.x - hitPadding,
        top = touchPoint.y - hitPadding,
        right = touchPoint.x + hitPadding,
        bottom = touchPoint.y + hitPadding
    )

    return findPathsAt(
        touchBounds,
        drawnPaths,
        rejectedPaths,
        canvasOffset,
        canvasScale,
        hitPadding,
        hitStep,
        false
    )
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
    val pathMeasure = PathMeasure()

    return drawnPaths.filter { drawnPath ->
        if (drawnPath in rejectedPaths) return@filter false

        val threshold = (drawnPath.strokeWidth / 1.25f) + hitPadding
        val logicalRect = inRectangle.inflate(threshold)

        if (drawnPath.drawingTool == DrawingTool.ERASER || !drawnPath.path.getBounds().overlaps(logicalRect)) return@filter false

        if (drawnPath.drawingTool.isFillable() && drawnPath.fillColor != Color.Transparent) {
            val bounds = drawnPath.path.getBounds()
            val region = android.graphics.Region()
            val boundsRegion = android.graphics.Region(
                bounds.left.toInt() - hitPadding.toInt(),
                bounds.top.toInt() - hitPadding.toInt(),
                bounds.right.toInt() + hitPadding.toInt(),
                bounds.bottom.toInt() + hitPadding.toInt()
            )
            region.setPath(drawnPath.path.asAndroidPath(), boundsRegion)

            if (!isMarquee) {
                val center = logicalRect.center
                region.contains(center.x.toInt(), center.y.toInt())
            } else {
                val selectionRegion = android.graphics.Region(
                    logicalRect.left.toInt(),
                    logicalRect.top.toInt(),
                    logicalRect.right.toInt(),
                    logicalRect.bottom.toInt()
                )
                region.op(
                    selectionRegion,
                    android.graphics.Region.Op.INTERSECT
                )
                !region.isEmpty
            }
        } else {
            pathMeasure.setPath(drawnPath.path, false)
            val length = pathMeasure.length

            if (length == 0f) return@filter drawnPath.path.getBounds().overlaps(logicalRect)

            var distance = 0f
            var hit = false
            while (distance <= length && !hit) {
                val pos = pathMeasure.getPosition(distance)
                if (pos.isIn(logicalRect)) hit = true
                distance += hitStep
            }

            if (!hit) {
                val endPos = pathMeasure.getPosition(length)
                hit = endPos.isIn(logicalRect)
            }

            hit
        }
    }
}

private fun Offset.isIn(rect: Rect): Boolean = (x in rect.left ..  rect.right && y in rect.top .. rect.bottom)