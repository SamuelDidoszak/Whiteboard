package org.samis.whiteboard.presentation.whiteboard.util

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import org.samis.whiteboard.presentation.util.rotateBy
import kotlin.math.roundToInt

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

    fun resize(handle: SelectionResizeHandle, drag: Offset): AddedPicture {
        val localDrag = drag.rotateBy(-rotation)
        val width: Int
        val height: Int

        if (handle.isCorner) {
            val diagonal = Offset(handle.horizontal * this.width.toFloat(), handle.vertical * this.height.toFloat())
            val lengthSquared = diagonal.x * diagonal.x + diagonal.y * diagonal.y
            val scale = if (lengthSquared == 0f) 1f else
                (1f + (localDrag.x * diagonal.x + localDrag.y * diagonal.y) / lengthSquared).coerceAtLeast(0f)
            width = (this.width * scale).roundToInt().coerceAtLeast(1)
            height = (this.height * scale).roundToInt().coerceAtLeast(1)
        } else {
            width = (this.width + handle.horizontal * localDrag.x.roundToInt()).coerceAtLeast(1)
            height = (this.height + handle.vertical * localDrag.y.roundToInt()).coerceAtLeast(1)
        }

        val oldCenter = position + Offset(this.width / 2f, this.height / 2f)
        val centerShift = Offset(
            handle.horizontal * (width - this.width) / 2f,
            handle.vertical * (height - this.height) / 2f
        ).rotateBy(rotation)

        return copy(
            position = oldCenter + centerShift - Offset(width / 2f, height / 2f),
            width = width,
            height = height
        )
    }
}