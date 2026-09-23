package org.samis.whiteboard.presentation.whiteboard.util

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import org.samis.whiteboard.domain.model.DrawnPath
import org.samis.whiteboard.presentation.util.rotateBy
import kotlin.math.roundToInt
import kotlin.math.sqrt

sealed class DrawnElement {
    abstract var id: Long?

    data class Path(val path: DrawnPath): DrawnElement() {
        override var id: Long?
            get() = path.id
            set(value) { path.id = value }
    }

    data class Picture(val picture: AddedPicture): DrawnElement() {
        override var id: Long?
            get() = picture.id
            set(value) { picture.id = value }
    }

    fun transform(from: SelectionFrame, to: SelectionFrame): DrawnElement {
        val scaleX = to.bounds.width / from.bounds.width.coerceAtLeast(1f)
        val scaleY = to.bounds.height / from.bounds.height.coerceAtLeast(1f)
        val rotationDelta = to.rotation - from.rotation

        fun transformPoint(point: Offset): Offset {
            val local = (point - from.center).rotateBy(-from.rotation)
            return to.center + Offset(local.x * scaleX, local.y * scaleY).rotateBy(to.rotation)
        }

        return when (this) {
            is DrawnElement.Path -> {
                val matrix = Matrix().apply {
                    translate(-from.center.x, -from.center.y)
                    rotateZ(-from.rotation)
                    scale(scaleX, scaleY)
                    rotateZ(to.rotation)
                    translate(to.center.x, to.center.y)
                }
                val transformedPath = Path().apply {
                    addPath(path.path)
                    transform(matrix)
                }
                Path(path.copy(
                    path = transformedPath,
                    strokeWidth = path.strokeWidth * sqrt(scaleX * scaleY)
                ))
            }
            is Picture -> {
                val oldCenter = picture.position + Offset(picture.width / 2f, picture.height / 2f)
                val width = (picture.width * scaleX).roundToInt().coerceAtLeast(1)
                val height = (picture.height * scaleY).roundToInt().coerceAtLeast(1)
                Picture(picture.copy(
                    position = transformPoint(oldCenter) - Offset(width / 2f, height / 2f),
                    width = width,
                    height = height,
                    rotation = picture.rotation + rotationDelta
                ))
            }
        }
    }
}