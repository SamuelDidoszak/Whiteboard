package org.samis.whiteboard.presentation.whiteboard

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.CoroutineScope
import org.samis.whiteboard.domain.model.ColorPaletteType
import org.samis.whiteboard.domain.model.DrawingTool
import org.samis.whiteboard.presentation.util.Palette

sealed class WhiteboardEvent {
    data class StartDrawing(val offset: Offset) : WhiteboardEvent()
    data class ContinueDrawing(val continuingOffset: Offset) : WhiteboardEvent()
    data object FinishDrawing : WhiteboardEvent()

    data class OnDrawingToolSelected(val drawingTool: DrawingTool) : WhiteboardEvent()

    data class StrokeSliderValueChange(val strokeWidth: Float) : WhiteboardEvent()
    data class OpacitySliderValueChange(val opacity: Float) : WhiteboardEvent()
    data class CanvasColorChange(val canvasColor: Color) : WhiteboardEvent()
    data class StrokeColorChange(val strokeColor: Color, val modifyColor: Boolean) : WhiteboardEvent()
    data class FillColorChange(val fillColor: Color) : WhiteboardEvent()

    data object OnCommandPaletteIconClick: WhiteboardEvent()
    data object OnCommandPaletteClose: WhiteboardEvent()
    data object OnPaletteEditMode: WhiteboardEvent()
    data class OnPaletteAdded(val palette: Palette): WhiteboardEvent()
    data class OnPaletteRemoved(val palette: Palette): WhiteboardEvent()
    data class ShowRemovePaletteDialog(val palette: Palette): WhiteboardEvent()
    data object HideRemovePaletteDialog: WhiteboardEvent()
    data class OnColorPaletteIconClick(val colorPaletteType: ColorPaletteType): WhiteboardEvent()
    data class OnColorSelected(val color: Color): WhiteboardEvent()
    data class OnColorDeleted(val color: Color, val colorPaletteType: ColorPaletteType): WhiteboardEvent()
    data class SetColorDeletionMode(val on: Boolean, val colorType: ColorPaletteType): WhiteboardEvent()

    data object ColorSelectionDialogDismiss : WhiteboardEvent()
    data object OnLaserPathAnimationComplete : WhiteboardEvent()
    data object OnCardClose: WhiteboardEvent()

    data object Undo: WhiteboardEvent()
    data object Redo: WhiteboardEvent()

    data class SetCaptureController(val captureController: Any): WhiteboardEvent()
    data class SavePicture(val scope: CoroutineScope): WhiteboardEvent()
    data class SaveMiniature(val scope: CoroutineScope): WhiteboardEvent()

    data class StrokeWidthButtonClicked(val strokeNum: Int): WhiteboardEvent()
    data object OnStrokeWidthSliderClose: WhiteboardEvent()

    data class OnPalettePicked(val palette: Palette): WhiteboardEvent()
}