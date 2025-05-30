package org.samis.whiteboard.presentation.whiteboard

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.CoroutineScope
import org.samis.whiteboard.domain.model.ColorPaletteType
import org.samis.whiteboard.domain.model.DrawingTool

sealed class WhiteboardEvent {
    data class StartDrawing(val offset: Offset) : WhiteboardEvent()
    data class ContinueDrawing(val continuingOffset: Offset) : WhiteboardEvent()
    data object FinishDrawing : WhiteboardEvent()

    data class OnDrawingToolSelected(val drawingTool: DrawingTool) : WhiteboardEvent()

    data class StrokeSliderValueChange(val strokeWidth: Float) : WhiteboardEvent()
    data class OpacitySliderValueChange(val opacity: Float) : WhiteboardEvent()
    data class CanvasColorChange(val canvasColor: Color) : WhiteboardEvent()
    data class StrokeColorChange(val strokeColor: Color, val modifyColor: Boolean) : WhiteboardEvent()
    data class FillColorChange(val backgroundColor: Color) : WhiteboardEvent()

    data class OnColorPaletteIconClick(val colorPaletteType: ColorPaletteType): WhiteboardEvent()
    data class OnColorSelected(val color: Color): WhiteboardEvent()
    data class OnColorDeleted(val color: Color, val colorPaletteType: ColorPaletteType): WhiteboardEvent()
    data class SetColorDeletionMode(val on: Boolean): WhiteboardEvent()

    data object ColorSelectionDialogDismiss : WhiteboardEvent()
    data object OnLaserPathAnimationComplete : WhiteboardEvent()
    data object OnCardClose: WhiteboardEvent()

    data object Undo: WhiteboardEvent()
    data object Redo: WhiteboardEvent()

    data class SetCaptureController(val captureController: Any): WhiteboardEvent()
    data class SavePicture(val scope: CoroutineScope): WhiteboardEvent()
}