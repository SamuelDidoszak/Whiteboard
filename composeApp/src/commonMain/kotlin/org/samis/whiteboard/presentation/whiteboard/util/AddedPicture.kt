package org.samis.whiteboard.presentation.whiteboard.util

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path

data class AddedPicture(
    var id: Long? = null,
    val picturePath: String,
    var position: Offset = Offset.Zero,
    var width: Int = 0,
    var height: Int = 0,
    var rotation: Float = 0f
) {
    fun getBoundsPath(): Path {
        val rect = Rect(
            left = position.x,
            top = position.y,
            right = position.x + width.toFloat(),
            bottom = position.y + height.toFloat(),
        )

        return Path().apply {
            addRect(rect)

            transform(
                Matrix().apply {
                    translate(-rect.center.x, -rect.center.y)
                    rotateZ(rotation)
                    translate(rect.center.x, rect.center.y)
                }
            )
        }
    }
}