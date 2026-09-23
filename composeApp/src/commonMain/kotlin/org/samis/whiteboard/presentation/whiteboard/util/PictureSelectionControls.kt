package org.samis.whiteboard.presentation.whiteboard.util

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import org.samis.whiteboard.presentation.whiteboard.WhiteboardEvent
import org.samis.whiteboard.presentation.whiteboard.WhiteboardState
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

private const val ROTATION_DISTANCE_DP = 20f
private const val ROTATION_RADIUS_DP = 16f
private const val ROTATION_SWEEP_DEGREES = 155f
private const val DELETE_INSET_DP = 20f

enum class PictureResizeHandle(val horizontal: Int, val vertical: Int) {
    TOP_LEFT(-1, -1),
    TOP(0, -1),
    TOP_RIGHT(1, -1),
    RIGHT(1, 0),
    BOTTOM_RIGHT(1, 1),
    BOTTOM(0, 1),
    BOTTOM_LEFT(-1, 1),
    LEFT(-1, 0);

    val isCorner get() = horizontal != 0 && vertical != 0
}

enum class PictureCorner(val outwardDiagonalAngle: Float) {
    TOP_LEFT(225f),
    TOP_RIGHT(315f),
    BOTTOM_RIGHT(45f),
    BOTTOM_LEFT(135f)
}

sealed interface PictureControl {
    data class Resize(val handle: PictureResizeHandle) : PictureControl
    data class Rotate(val corner: PictureCorner) : PictureControl
    data object Delete : PictureControl
}

data class PictureControlHit(val picture: AddedPicture, val control: PictureControl)

private data class ResizeControl(
    val handle: PictureResizeHandle,
    val center: Offset
)

private data class RotationControl(
    val corner: PictureCorner,
    val arcMidpoint: Offset,
    val outwardDiagonalAngle: Float
)

private data class PictureControlLayout(
    val resizeControls: List<ResizeControl>,
    val rotationControls: List<RotationControl>,
    val deleteCenter: Offset
)

private fun AddedPicture.getControlLayout(
    rotationControlDistance: Float,
    deleteInset: Float
): PictureControlLayout {
    val left = position.x
    val top = position.y
    val right = left + width.toFloat()
    val bottom = top + height.toFloat()
    val center = Offset(left + width / 2f, top + height / 2f)

    fun transform(point: Offset): Offset = point.rotateAround(center, rotation)

    val resizeControls = listOf(
        ResizeControl(PictureResizeHandle.TOP_LEFT, transform(Offset(left, top))),
        ResizeControl(PictureResizeHandle.TOP, transform(Offset(center.x, top))),
        ResizeControl(PictureResizeHandle.TOP_RIGHT, transform(Offset(right, top))),
        ResizeControl(PictureResizeHandle.RIGHT, transform(Offset(right, center.y))),
        ResizeControl(PictureResizeHandle.BOTTOM_RIGHT, transform(Offset(right, bottom))),
        ResizeControl(PictureResizeHandle.BOTTOM, transform(Offset(center.x, bottom))),
        ResizeControl(PictureResizeHandle.BOTTOM_LEFT, transform(Offset(left, bottom))),
        ResizeControl(PictureResizeHandle.LEFT, transform(Offset(left, center.y)))
    )

    val diagonalOffset = rotationControlDistance / kotlin.math.sqrt(2f)
    val rotationControls = listOf(
        RotationControl(
            corner = PictureCorner.TOP_LEFT,
            arcMidpoint = transform(Offset(left - diagonalOffset, top - diagonalOffset)),
            outwardDiagonalAngle = PictureCorner.TOP_LEFT.outwardDiagonalAngle + rotation
        ),
        RotationControl(
            corner = PictureCorner.TOP_RIGHT,
            arcMidpoint = transform(Offset(right + diagonalOffset, top - diagonalOffset)),
            outwardDiagonalAngle = PictureCorner.TOP_RIGHT.outwardDiagonalAngle + rotation
        ),
        RotationControl(
            corner = PictureCorner.BOTTOM_RIGHT,
            arcMidpoint = transform(Offset(right + diagonalOffset, bottom + diagonalOffset)),
            outwardDiagonalAngle = PictureCorner.BOTTOM_RIGHT.outwardDiagonalAngle + rotation
        ),
        RotationControl(
            corner = PictureCorner.BOTTOM_LEFT,
            arcMidpoint = transform(Offset(left - diagonalOffset, bottom + diagonalOffset)),
            outwardDiagonalAngle = PictureCorner.BOTTOM_LEFT.outwardDiagonalAngle + rotation
        )
    )

    val deleteCenter = transform(Offset(right - deleteInset, top + deleteInset))

    return PictureControlLayout(resizeControls, rotationControls, deleteCenter)
}

fun Density.hitTestPictureControls(
    pictures: List<AddedPicture>,
    screenPosition: Offset,
    canvasOffset: Offset,
    canvasScale: Float
): PictureControlHit? {
    val zoom = canvasScale.coerceAtLeast(0.001f)
    val logicalPosition = (screenPosition - canvasOffset) / zoom
    val layoutDistance = pictureChromeDp(ROTATION_DISTANCE_DP, zoom)
    val deleteInset = pictureChromeDp(DELETE_INSET_DP, zoom)
    val resizeHitRadius = pictureChromeDp(14f, zoom)
    val deleteHitRadius = pictureChromeDp(13f, zoom)
    val rotationHitWidth = pictureChromeDp(11f, zoom)
    val rotationRadius = pictureChromeDp(ROTATION_RADIUS_DP, zoom)

    for (picture in pictures.asReversed()) {
        if (picture.width <= 0 || picture.height <= 0) continue
        val layout = picture.getControlLayout(layoutDistance, deleteInset)

        if ((logicalPosition - layout.deleteCenter).getDistance() <= deleteHitRadius) {
            return PictureControlHit(picture, PictureControl.Delete)
        }

        layout.resizeControls.firstOrNull {
            (logicalPosition - it.center).getDistance() <= resizeHitRadius
        }?.let {
            return PictureControlHit(picture, PictureControl.Resize(it.handle))
        }

        layout.rotationControls.firstOrNull { control ->
            val center = control.arcMidpoint - directionVector(control.outwardDiagonalAngle, rotationRadius)
            val fromCenter = logicalPosition - center
            val angle = atan2(fromCenter.y, fromCenter.x) * 180f / PI.toFloat()
            val startAngle = control.outwardDiagonalAngle - ROTATION_SWEEP_DEGREES / 2f
            val angleFromStart = ((angle - startAngle) % 360f + 360f) % 360f
            val onArc = angleFromStart <= ROTATION_SWEEP_DEGREES &&
                abs(fromCenter.getDistance() - rotationRadius) <= rotationHitWidth
            val nearStart = (logicalPosition - pointOnCircle(center, rotationRadius, startAngle)).getDistance() <= rotationHitWidth
            val nearEnd = (logicalPosition - pointOnCircle(center, rotationRadius, startAngle + ROTATION_SWEEP_DEGREES)).getDistance() <= rotationHitWidth
            onArc || nearStart || nearEnd
        }?.let {
            return PictureControlHit(picture, PictureControl.Rotate(it.corner))
        }
    }

    return null
}

private fun Offset.rotateAround(center: Offset, degrees: Float): Offset {
    if (degrees == 0f) return this

    val radians = degrees.toDouble() * PI / 180.0
    val cos = cos(radians).toFloat()
    val sin = sin(radians).toFloat()
    val dx = x - center.x
    val dy = y - center.y

    return Offset(
        x = center.x + dx * cos - dy * sin,
        y = center.y + dx * sin + dy * cos
    )
}

fun DrawScope.drawPictureSelectionBounds(
    picture: AddedPicture,
    canvasScale: Float
) {
    if (picture.width <= 0 || picture.height <= 0) return

    fun screenDp(value: Float): Float = pictureChromeDp(value, canvasScale)

    val center = picture.position + Offset(picture.width / 2f, picture.height / 2f)
    val accent = Color(0xFF50DEFF)
    val cornerRadius = CornerRadius(screenDp(2.5f))
    val bounds = RoundRect(
        left = picture.position.x,
        top = picture.position.y,
        right = picture.position.x + picture.width,
        bottom = picture.position.y + picture.height,
        cornerRadius = cornerRadius
    )
    val interior = Path().apply { addRoundRect(bounds) }

    rotate(degrees = picture.rotation, pivot = center) {
        clipPath(interior, clipOp = ClipOp.Difference) {
            listOf(
                8f to 0.05f,
                7f to 0.20f,
                5f to 0.30f,
                3f to 0.45f,
                1.5f to 0.70f
            ).forEach { (width, alpha) ->
                drawRoundRect(
                    color = accent.copy(alpha = alpha),
                    topLeft = picture.position,
                    size = Size(picture.width.toFloat(), picture.height.toFloat()),
                    cornerRadius = cornerRadius,
                    style = Stroke(
                        width = screenDp(width),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }
    }
}

fun DrawScope.drawPictureSelectionControls(
    picture: AddedPicture,
    canvasScale: Float
) {
    if (picture.width <= 0 || picture.height <= 0) return

    fun screenDp(value: Float): Float = pictureChromeDp(value, canvasScale)

    val accent = Color(0xFF50DEFF)
    val layout = picture.getControlLayout(
        rotationControlDistance = screenDp(ROTATION_DISTANCE_DP),
        deleteInset = screenDp(DELETE_INSET_DP)
    )

    layout.resizeControls.forEach { control ->
        val radius = screenDp(if (control.handle.isCorner) 5.5f else 4.5f)
        drawCircle(
            color = Color.Black.copy(alpha = 0.20f),
            radius = radius + screenDp(1f),
            center = control.center
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.96f),
            radius = radius,
            center = control.center
        )
        drawCircle(
            color = accent,
            radius = radius,
            center = control.center,
            style = Stroke(width = screenDp(1.5f))
        )
    }

    layout.rotationControls.forEach { control ->
        drawRotationControl(
            arcMidpoint = control.arcMidpoint,
            outwardDiagonalAngle = control.outwardDiagonalAngle,
            radius = screenDp(ROTATION_RADIUS_DP),
            strokeWidth = screenDp(2.3f),
            arrowLength = screenDp(8f),
            color = accent
        )
    }

    drawDeleteControl(
        center = layout.deleteCenter,
        halfSize = screenDp(6f),
        strokeWidth = screenDp(2f)
    )
}

private fun Density.pictureChromeDp(value: Float, canvasScale: Float): Float {
    val zoom = canvasScale.coerceAtLeast(0.001f)
    return value.dp.toPx() * zoom.pow(0.3f) / zoom
}

private fun DrawScope.drawRotationControl(
    arcMidpoint: Offset,
    outwardDiagonalAngle: Float,
    radius: Float,
    strokeWidth: Float,
    arrowLength: Float,
    color: Color
) {
    val center = arcMidpoint - directionVector(outwardDiagonalAngle, radius)
    val sweepAngle = ROTATION_SWEEP_DEGREES
    val startAngle = outwardDiagonalAngle - sweepAngle / 2f
    val endAngle = startAngle + sweepAngle
    val arcBounds = center - Offset(radius, radius)
    val startPoint = pointOnCircle(center, radius, startAngle)
    val endPoint = pointOnCircle(center, radius, endAngle)

    fun drawArcStroke(arcColor: Color, width: Float) {
        drawArc(
            color = arcColor,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = arcBounds,
            size = Size(radius * 2f, radius * 2f),
            style = Stroke(width = width, cap = StrokeCap.Butt)
        )
    }

    val haloColor = Color.White.copy(alpha = 0.65f)
    drawArcStroke(haloColor, strokeWidth * 2.4f)
    drawArrowHead(startPoint, startAngle - 90f, arrowLength, strokeWidth * 2.4f, haloColor)
    drawArrowHead(endPoint, endAngle + 90f, arrowLength, strokeWidth * 2.4f, haloColor)

    val foregroundColor = color.copy(alpha = 0.95f)
    drawArcStroke(foregroundColor, strokeWidth)
    drawArrowHead(startPoint, startAngle - 90f, arrowLength, strokeWidth, foregroundColor)
    drawArrowHead(endPoint, endAngle + 90f, arrowLength, strokeWidth, foregroundColor)
}

private fun DrawScope.drawArrowHead(
    tip: Offset,
    directionAngle: Float,
    length: Float,
    strokeWidth: Float,
    color: Color
) {
    val spread = 45f
    val left = tip - directionVector(directionAngle - spread, length)
    val right = tip - directionVector(directionAngle + spread, length)

    val arrowHead = Path().apply {
        moveTo(left.x, left.y)
        lineTo(tip.x, tip.y)
        lineTo(right.x, right.y)
    }
    drawPath(
        path = arrowHead,
        color = color,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Butt,
            join = StrokeJoin.Miter
        )
    )
}

private fun directionVector(angle: Float, length: Float): Offset {
    val radians = angle.toDouble() * PI / 180.0
    return Offset(cos(radians).toFloat() * length, sin(radians).toFloat() * length)
}

private fun pointOnCircle(center: Offset, radius: Float, angle: Float): Offset {
    val radians = angle.toDouble() * PI / 180.0
    return center + Offset(cos(radians).toFloat() * radius, sin(radians).toFloat() * radius)
}

private fun DrawScope.drawDeleteControl(
    center: Offset,
    halfSize: Float,
    strokeWidth: Float
) {
    val topLeft = center - Offset(halfSize, halfSize)
    val bottomRight = center + Offset(halfSize, halfSize)
    val topRight = center + Offset(halfSize, -halfSize)
    val bottomLeft = center + Offset(-halfSize, halfSize)

    fun drawX(color: Color, width: Float) {
        drawLine(color, topLeft, bottomRight, width, cap = StrokeCap.Round)
        drawLine(color, topRight, bottomLeft, width, cap = StrokeCap.Round)
    }

    drawX(Color.Black.copy(alpha = 0.24f), strokeWidth * 2.4f)
    drawX(Color.White.copy(alpha = 0.68f), strokeWidth)
}


fun Density.pictureControlAt(state: WhiteboardState, screenPosition: Offset): PictureControlHit? =
    hitTestPictureControls(
        pictures = state.selectedElements.filterIsInstance<DrawnElement.Picture>().map { it.picture },
        screenPosition = screenPosition,
        canvasOffset = state.canvasOffset,
        canvasScale = state.canvasScale
    )

suspend fun PointerInputScope.detectPictureControlGestures(
    stateProvider: () -> WhiteboardState,
    onEvent: (WhiteboardEvent) -> Unit
) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
        val stateAtDown = stateProvider()
        val hit = pictureControlAt(stateAtDown, down.position) ?: return@awaitEachGesture
        val zoom = stateAtDown.canvasScale.coerceAtLeast(0.001f)
        val canvasOffset = stateAtDown.canvasOffset
        fun logicalPosition(screenPosition: Offset): Offset = (screenPosition - canvasOffset) / zoom

        down.consume()
        var previousPosition = down.position
        var dragging = false

        try {
            while (true) {
                val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                if (event.changes.count { it.pressed } > 1) {
                    if (dragging) {
                        onEvent(WhiteboardEvent.PictureControlDragCancelled(hit.picture, hit.control))
                    }
                    break
                }

                val change = event.changes.firstOrNull { it.id == down.id }
                if (change == null) {
                    if (dragging) {
                        onEvent(WhiteboardEvent.PictureControlDragCancelled(hit.picture, hit.control))
                    }
                    break
                }

                val currentPosition = change.position
                if (!change.pressed) {
                    change.consume()
                    if (dragging) {
                        onEvent(
                            WhiteboardEvent.PictureControlDragEnded(
                                hit.picture, hit.control, logicalPosition(currentPosition)
                            )
                        )
                    } else if ((currentPosition - down.position).getDistance() <= viewConfiguration.touchSlop) {
                        onEvent(
                            WhiteboardEvent.PictureControlClicked(
                                hit.picture, hit.control, logicalPosition(currentPosition)
                            )
                        )
                    }
                    break
                }

                change.consume()
                if (!dragging && hit.control != PictureControl.Delete &&
                    (currentPosition - down.position).getDistance() > viewConfiguration.touchSlop
                ) {
                    dragging = true
                    onEvent(
                        WhiteboardEvent.PictureControlDragStarted(
                            hit.picture, hit.control, logicalPosition(down.position)
                        )
                    )
                    onEvent(
                        WhiteboardEvent.PictureControlDragged(
                            hit.picture, hit.control, logicalPosition(currentPosition),
                            (currentPosition - down.position) / zoom
                        )
                    )
                } else if (dragging && currentPosition != previousPosition) {
                    onEvent(
                        WhiteboardEvent.PictureControlDragged(
                            hit.picture, hit.control, logicalPosition(currentPosition),
                            (currentPosition - previousPosition) / zoom
                        )
                    )
                }
                previousPosition = currentPosition
            }
        } catch (cancellation: CancellationException) {
            if (dragging) {
                onEvent(WhiteboardEvent.PictureControlDragCancelled(hit.picture, hit.control))
            }
            throw cancellation
        }
    }
}
