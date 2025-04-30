package org.samis.whiteboard.data.mapper

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import org.samis.whiteboard.data.database.entity.WhiteboardEntity
import org.samis.whiteboard.domain.model.Whiteboard

fun Whiteboard.toWhiteboardEntity(): WhiteboardEntity {
    return WhiteboardEntity(
        id = id,
        name = name,
        lastEdited = lastEdited,
        canvasColor = canvasColor.toArgb(),
        pointer = pointer
    )
}

fun WhiteboardEntity.toWhiteboard(): Whiteboard {
    return Whiteboard(
        id = id,
        name = name,
        lastEdited = lastEdited,
        canvasColor = Color(canvasColor),
        pointer = pointer
    )
}

fun List<WhiteboardEntity>.toWhiteboardList() = this.map { it.toWhiteboard() }