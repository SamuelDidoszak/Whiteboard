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
import org.samis.whiteboard.presentation.util.Palette

data class WhiteboardState(
    val updates: List<Update> = emptyList(),
    val undoArray: List<Update> = emptyList(),
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
    val showOpacitySlider: Boolean = false,
    val showRemovePaletteDialog: Boolean = false,

    val selectedMarker: Int = 0,
    val opacity: Float = 100f,

    val isCommandPaletteOpen: Boolean = false,
    val isPaletteEditMode: Boolean = false,
    val selectedColorPaletteType: ColorPaletteType = ColorPaletteType.STROKE,
    val markerColorDeletionMode: Boolean = false,
    val canvasColorDeletionMode: Boolean = false,
    val canvasColor: Color = Color.White,
    val strokeColor: Color = Color.Black,
    val fillColor: Color = Color.Transparent,

    val whiteboardName: String = "Untitled",
    val miniatureSrc: String? = null,
    val markerColors: List<Color> = defaultDrawingColors,
    val strokeWidthList: List<Float> = listOf(1.8f, 5f, 10f),
    val activeStrokeWidthButton: Int = 1,

    val paletteList: List<Palette> = listOf(),
    val paletteToDelete: Palette? = null,
    val preferredStrokeColors: List<Color> = defaultDrawingColors,
    val preferredFillColors: List<Color> = defaultDrawingColors,
    val preferredCanvasColors: List<Color> = defaultCanvasColors,
    val stylusInput: Boolean = false,

    val captureController: Any? = null
) {
    val strokeWidth
        get() = strokeWidthList[activeStrokeWidthButton]
    
    val palette: Palette
        get() {
            var otherColors = listOf<Color>()
            if (preferredStrokeColors.size > 4)
                otherColors = preferredStrokeColors.subList(4, preferredStrokeColors.size)
            return Palette(canvasColor, preferredStrokeColors[0], preferredStrokeColors[1], preferredStrokeColors[2], preferredStrokeColors[3], otherColors)
        }
}
