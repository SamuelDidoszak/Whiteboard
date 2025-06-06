package org.samis.whiteboard.domain.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path

data class DrawnPath(
    var id: Long? = null,
    val path: Path,
    val drawingTool: DrawingTool,
    val strokeWidth: Float,
    var strokeColor: Color,
    val fillColor: Color,
    val opacity: Float
)
