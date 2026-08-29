package org.samis.whiteboard.domain.model

import org.jetbrains.compose.resources.DrawableResource
import whiteboard.composeapp.generated.resources.PanZoom
import whiteboard.composeapp.generated.resources.Pen
import whiteboard.composeapp.generated.resources.Res
import whiteboard.composeapp.generated.resources.dotted_line
import whiteboard.composeapp.generated.resources.ic_arrow
import whiteboard.composeapp.generated.resources.ic_circle
import whiteboard.composeapp.generated.resources.ic_laser_pointer
import whiteboard.composeapp.generated.resources.ic_line
import whiteboard.composeapp.generated.resources.ic_rectangle
import whiteboard.composeapp.generated.resources.ic_triangle
import whiteboard.composeapp.generated.resources.img_eraser
import whiteboard.composeapp.generated.resources.img_highlighter

enum class DrawingTool(
    val res: DrawableResource
) {
    PEN(res = Res.drawable.Pen),
    LASER_PEN(res = Res.drawable.ic_laser_pointer),
    DASHER(res = Res.drawable.dotted_line),
    HIGHLIGHTER(res = Res.drawable.img_highlighter),
    DELETER(res = Res.drawable.img_eraser),
    ERASER(res = Res.drawable.img_eraser),
//    MARQUEE(res = Res.drawable.Select),
    CANVAS_PANNER(res = Res.drawable.PanZoom),
    LINE(res = Res.drawable.ic_line),
    ARROW(res = Res.drawable.ic_arrow),
    RECTANGLE(res = Res.drawable.ic_rectangle),
    CIRCLE(res = Res.drawable.ic_circle),
    TRIANGLE(res = Res.drawable.ic_triangle);

    fun isFillable(): Boolean {
        return when (this) {
            RECTANGLE, CIRCLE, TRIANGLE -> true
            else -> false
        }
    }

    fun isSmoothable(): Boolean {
        return when (this) {
            PEN, ERASER, HIGHLIGHTER, DASHER -> true
            else -> false
        }
    }

    fun isErasing(): Boolean {
        return when (this) {
            ERASER, DELETER -> true
            else -> false
        }
    }
}