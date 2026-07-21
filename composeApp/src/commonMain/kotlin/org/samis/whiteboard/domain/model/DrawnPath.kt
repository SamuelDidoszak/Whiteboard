package org.samis.whiteboard.domain.model

import androidx.compose.ui.geometry.Offset
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
) {
    fun translate(offset: Offset): DrawnPath {
        val newPath = Path().apply {
            addPath(path)
            translate(offset)
        }
        return copy(path = newPath)
    }
}
