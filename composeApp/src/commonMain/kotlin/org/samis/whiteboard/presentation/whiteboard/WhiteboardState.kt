package org.samis.whiteboard.presentation.whiteboard

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import org.samis.whiteboard.domain.model.ColorPaletteType
import org.samis.whiteboard.domain.model.DrawingTool
import org.samis.whiteboard.domain.model.DrawnPath
import org.samis.whiteboard.domain.model.Update
import org.samis.whiteboard.presentation.theme.defaultCanvasColors
import org.samis.whiteboard.presentation.theme.defaultDrawingColors
import org.samis.whiteboard.presentation.util.DrawingToolVisibility

data class WhiteboardState(
    val updates: List<Update> = emptyList(),
    val updatePointer: Int? = null,

    val paths: List<DrawnPath> = emptyList(),
    val pathsToBeDeleted: List<DrawnPath> = emptyList(),
    var currentPath: DrawnPath? = null,
    val laserPenPath: DrawnPath? = null,

    val startingOffset: Offset = Offset.Zero,
    val selectedDrawingTool: DrawingTool = DrawingTool.PEN,
    val drawingToolVisibility: DrawingToolVisibility = DrawingToolVisibility(),

    val isColorSelectionDialogOpen: Boolean = false,
    val isColorPickerOpen: Boolean = false,
    val isStrokeWidthSliderOpen: Boolean = false,

    val selectedMarker: Int = 0,
    val strokeWidth: Float = 5f,
    val opacity: Float = 100f,

    val selectedColorPaletteType: ColorPaletteType = ColorPaletteType.STROKE,
    val colorDeletionMode: Boolean = false,
    val canvasColor: Color = Color.White,
    val strokeColor: Color = Color.Black,
    val fillColor: Color = Color.Transparent,

    val whiteboardName: String = "Untitled",
    val markerColors: List<Color> = defaultDrawingColors,

    val preferredStrokeColors: List<Color> = defaultDrawingColors,
    val preferredFillColors: List<Color> = defaultDrawingColors,
    val preferredCanvasColors: List<Color> = defaultCanvasColors,

    val captureController: Any? = null
)
