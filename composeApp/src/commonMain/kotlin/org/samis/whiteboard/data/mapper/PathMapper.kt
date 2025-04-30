package org.samis.whiteboard.data.mapper

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import org.samis.whiteboard.data.database.entity.PathEntity
import org.samis.whiteboard.domain.model.DrawnPath

fun DrawnPath.toPathEntity(): PathEntity {
    return PathEntity(
        id = id,
        drawingTool = drawingTool,
        path = path,
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
        path = path,
        strokeWidth = strokeWidth,
        opacity = opacity,
        strokeColor = Color(strokeColor),
        fillColor = Color(fillColor)
    )
}

fun List<PathEntity>.toDrawnPathList() = map { it.toDrawnPath() }