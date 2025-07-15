package org.samis.whiteboard.data.mapper

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import org.samis.whiteboard.data.database.entity.WhiteboardEntity
import org.samis.whiteboard.domain.model.Whiteboard
import org.samis.whiteboard.presentation.util.Palette

fun Whiteboard.toWhiteboardEntity(): WhiteboardEntity {
    return WhiteboardEntity(
        id = id,
        name = name,
        createTime = createTime,
        lastModified = lastModified,
        palette = palette.toIntList(),
        markerColors = markerColors.map { it.toArgb() },
        strokeWidths = strokeWidths,
        activeStrokeWidthButton = activeStrokeWidthButton,
        opacity = opacity,
        fillColor = fillColor.toArgb(),
        pointer = pointer,
        miniatureSrc = miniatureSrc
    )
}

fun WhiteboardEntity.toWhiteboard(): Whiteboard {
    return Whiteboard(
        id = id,
        name = name,
        createTime = createTime,
        lastModified = lastModified,
        palette = palette.toPalette(),
        markerColors = markerColors.map { Color(it) },
        strokeWidths = strokeWidths,
        activeStrokeWidthButton = activeStrokeWidthButton,
        opacity = opacity,
        fillColor = Color(fillColor),
        pointer = pointer,
        miniatureSrc = miniatureSrc
    )
}

fun List<WhiteboardEntity>.toWhiteboardList() = this.map { it.toWhiteboard() }

private fun Palette.toIntList() = listOf(
    this.background.toArgb(),
    this.foreground.toArgb(),
    this.red.toArgb(),
    this.blue.toArgb(),
    this.green.toArgb()
) + this.others.map { it.toArgb() }

private fun List<Int>.toPalette(): Palette {
    var others = listOf<Int>()
    if (this.size > 4)
        others = this.subList(5, this.size)

    return Palette(Color(this[0]), Color(this[1]), Color(this[2]),
        Color(this[3]), Color(this[4]), others.map { Color(it) })
}