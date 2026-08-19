package org.samis.whiteboard.data.mapper

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.toArgb
import org.samis.whiteboard.data.database.entity.PathEntity
import org.samis.whiteboard.domain.model.DrawingTool
import org.samis.whiteboard.domain.model.DrawnPath
import kotlin.math.max
import kotlin.math.min

fun DrawnPath.toPathEntity(points: List<Offset>): PathEntity {
    return PathEntity(
        id = id,
        drawingTool = drawingTool,
        points = pointsToString(points),
        strokeWidth = strokeWidth,
        opacity = opacity,
        strokeColor = strokeColor.toArgb(),
        fillColor = fillColor.toArgb()
    )
}

fun PathEntity.toDrawnPath(): DrawnPath {
    return DrawnPath(
        id = id,
        drawingTool = drawingTool,
        path = pathFromPointList(points.toPoints(), drawingTool),
        strokeWidth = strokeWidth,
        opacity = opacity,
        strokeColor = Color(strokeColor),
        fillColor = Color(fillColor)
    )
}

private fun pointsToString(points: List<Offset>): String {
    if (points.isEmpty()) return ""
    return points.joinToString(";") { "${it.x},${it.y}" }
}

private fun String.toPoints(): List<Offset> {
    if (this.isBlank()) return emptyList()
    return this.split(";").map { pair ->
        val (x, y) = pair.split(",")
        Offset(x.toFloat(), y.toFloat())
    }
}

private fun pathFromPointList(points: List<Offset>, drawingTool: DrawingTool): Path {
    if (points.isEmpty()) return Path()
    return when (drawingTool) {
        DrawingTool.PEN, DrawingTool.ERASER, DrawingTool.HIGHLIGHTER, DrawingTool.DASHER ->
            Path().apply {
                moveTo(points.first().x, points.first().y)
                for (i in 1 until points.size - 1) {
                    val mid = Offset(
                        x = (points[i].x + points[i + 1].x) / 2f,
                        y = (points[i].y + points[i + 1].y) / 2f
                    )
                    quadraticBezierTo(points[i].x, points[i].y, mid.x, mid.y)
                }
                lineTo(points.last().x, points.last().y)
            }

        DrawingTool.LINE, DrawingTool.ARROW ->
            Path().apply {
                moveTo(points.first().x, points.first().y)
                lineTo(points.last().x, points.last().y)
            }

        DrawingTool.RECTANGLE -> {
            val start = points.first()
            val offset = points.last()

            val topLeft = Offset(min(start.x, offset.x), min(start.y, offset.y))
            val bottomRight = Offset(max(start.x, offset.x), max(start.y, offset.y))
            Path().apply {
                addRect(Rect(topLeft, bottomRight))
            }
        }

        DrawingTool.TRIANGLE -> {
            val start = points.first()
            val offset = points.last()
            val height = offset.y - start.y
            val baseWidth = offset.x - start.x
            val remainingVertex = Offset(x = start.x - baseWidth, y = start.y + height)

            Path().apply {
                moveTo(start.x, start.y)
                lineTo(offset.x, offset.y)
                lineTo(remainingVertex.x, remainingVertex.y)
                close()
            }
        }

        DrawingTool.CIRCLE -> {
            val width = points.last().x - points.first().x
            val height = points.last().y - points.first().y
            Path().apply {
                addOval(Rect(offset = points.first(), size = Size(width = width, height = height)))
            }
        }
        else -> Path()
    }
}