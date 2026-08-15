package org.samis.whiteboard.data.mapper

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.toArgb
import org.samis.whiteboard.data.database.entity.PathEntity
import org.samis.whiteboard.domain.model.DrawnPath

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
        path = pathFromPointList(points.toPoints()),
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

private fun pathFromPointList(points: List<Offset>): Path {
    if (points.isEmpty()) return Path()
    return Path().apply {
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
}