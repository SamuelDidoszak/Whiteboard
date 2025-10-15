package org.samis.whiteboard.presentation.whiteboard

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.samis.whiteboard.domain.model.ColorPaletteType
import org.samis.whiteboard.domain.model.DrawingTool
import org.samis.whiteboard.domain.model.DrawnPath
import org.samis.whiteboard.domain.model.Update
import org.samis.whiteboard.domain.model.Whiteboard
import org.samis.whiteboard.domain.repository.PathRepository
import org.samis.whiteboard.domain.repository.SettingsRepository
import org.samis.whiteboard.domain.repository.UpdateRepository
import org.samis.whiteboard.domain.repository.WhiteboardRepository
import org.samis.whiteboard.presentation.navigation.Routes
import org.samis.whiteboard.presentation.util.AppScope
import org.samis.whiteboard.presentation.util.DelayedTask
import org.samis.whiteboard.presentation.util.DrawingToolVisibility
import org.samis.whiteboard.presentation.util.IContextProvider
import org.samis.whiteboard.presentation.util.capture
import org.samis.whiteboard.presentation.util.formatDate
import org.samis.whiteboard.presentation.util.roundTo
import java.io.File
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

class WhiteboardViewModel(
    private val pathRepository: PathRepository,
    private val updateRepository: UpdateRepository,
    private val whiteboardRepository: WhiteboardRepository,
    private val settingsRepository: SettingsRepository,
    savedStateHandle: SavedStateHandle,
    private val contextProvider: IContextProvider
) : ViewModel() {

    private val whiteboardId = savedStateHandle.toRoute<Routes.WhiteboardScreen>().whiteboardId
    private var canUndo = true
    private var isFirstPath = true

    private var updatedWhiteboardId = MutableStateFlow(whiteboardId)
    private var updateMiniature = false
    private val updateMiniatureTask = DelayedTask(AppScope.scope) {
        onEvent(WhiteboardEvent.SaveMiniature(viewModelScope))
    }

    private val _state = MutableStateFlow(WhiteboardState())
    val state = combine(
        _state,
        settingsRepository.getPreferredCanvasColors(),
        settingsRepository.getDrawingToolVisibility(),
        settingsRepository.getStylusInput()
    ){ flows ->
        val state = flows[0] as WhiteboardState
        canUndo = true
        state.copy(
            preferredCanvasColors = flows[1] as List<Color>,
            drawingToolVisibility = flows[2] as DrawingToolVisibility,
            stylusInput = flows[3] as Boolean
        )
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = WhiteboardState()
    )

    init {
        whiteboardId?.let { id ->
            initializeWhiteboardById(id)
        }
        initializeUpdates(updatedWhiteboardId.value ?: -1)
    }

    fun onEvent(event: WhiteboardEvent) {
        when (event) {
            is WhiteboardEvent.StartDrawing -> {
                if (isFirstPath) {
                    if (state.value.whiteboardName == "Untitled")
                        _state.update { it.copy(whiteboardName = initializeWhiteboardName(translatePolish = true)) }

                    upsertWhiteboard()
                    isFirstPath = false
                }
                _state.update { it.copy(startingOffset = event.offset) }
                updateMiniature = false
            }

            is WhiteboardEvent.ContinueDrawing -> {
                updateContinuingOffsets(event.continuingOffset)
            }

            WhiteboardEvent.FinishDrawing -> {
                state.value.currentPath?.let { drawnPath ->
                    when (drawnPath.drawingTool) {
                        DrawingTool.DELETER -> {
                            // Deleter removes lines on the go. Code for DELETER logic is in the updateContinuingOffsets() method
                        }

                        DrawingTool.ERASER -> {
                            drawnPath.strokeColor = _state.value.canvasColor
                            insertPathAndUpdate(
                                drawnPath,
                                Update.Erase(drawnPath, whiteboardId = updatedWhiteboardId.value)
                            )
                            _state.update { it.copy(selectedDrawingTool = DrawingTool.PEN) }
                        }

                        DrawingTool.LASER_PEN -> {
                            _state.update { it.copy(laserPenPath = drawnPath) }
                        }

                        else -> {
                            insertPathAndUpdate(
                                drawnPath,
                                Update.AddPath(drawnPath, whiteboardId = updatedWhiteboardId.value)
                            )
                        }
                    }
                }

                _state.update {
                    it.copy(
                        // removes flickering
                        paths =
                            if (it.selectedDrawingTool != DrawingTool.LASER_PEN && it.selectedDrawingTool != DrawingTool.DELETER && it.currentPath != null)
                                it.paths.plus(it.currentPath!!)
                            else it.paths,
                        currentPath = null,
                        pathsToBeDeleted = emptyList()
                    )
                }
            }

            is WhiteboardEvent.OnDrawingToolSelected -> {
                when (event.drawingTool) {
                    DrawingTool.RECTANGLE, DrawingTool.CIRCLE, DrawingTool.TRIANGLE -> {
                        _state.update {
                            it.copy(selectedDrawingTool = event.drawingTool)
                        }
                    }

                    else -> {
                        _state.update {
                            it.copy(
                                selectedDrawingTool = event.drawingTool,
                                fillColor = Color.Transparent
                            )
                        }
                    }
                }
            }

            is WhiteboardEvent.FillColorChange -> {
                _state.update { it.copy(fillColor = event.fillColor) }
                upsertWhiteboard()
            }

            is WhiteboardEvent.OpacitySliderValueChange -> {
                _state.update { it.copy(opacity = event.opacity) }
                upsertWhiteboard()
            }

            is WhiteboardEvent.CanvasColorChange -> {
                _state.value.updates.forEach {
                    if (it is Update.Erase) {
                        it.path.strokeColor = event.canvasColor
                    }
                }
                _state.update { it.copy(
                    canvasColor = event.canvasColor,
                    updates = it.updates
                ) }
                upsertWhiteboard()

                updateMiniature = true
                updateMiniatureTask.start(4000)
            }

            is WhiteboardEvent.StrokeColorChange -> {
                if (state.value.selectedDrawingTool == DrawingTool.ERASER || state.value.selectedDrawingTool == DrawingTool.DELETER)
                    _state.update { it.copy(selectedDrawingTool = DrawingTool.PEN) }

                var markerNum = state.value.markerColors.indexOf(event.strokeColor)
                if (markerNum == -1 || markerNum > 3)
                    markerNum = state.value.markerColors.indexOf(state.value.strokeColor)

                if (event.strokeColor == state.value.strokeColor) {
                    val open = !state.value.isColorPickerOpen
                    _state.update {
                        it.copy(
                            isColorPickerOpen = open,
                            selectedMarker = markerNum
                        ) }
                    return
                }
                if (event.modifyColor) {
                    val markerColors = state.value.markerColors.mapIndexed { index, color ->
                        if (index == markerNum)
                            event.strokeColor
                        else
                            color
                    }
                    savePreferredColors(markerColors, ColorPaletteType.MARKER)
                }
                _state.update {
                    it.copy(
                        strokeColor = event.strokeColor,
                        selectedMarker = markerNum,
                        isColorPickerOpen = false,
                    )
                }
                if (!isFirstPath)
                    upsertWhiteboard()
            }

            WhiteboardEvent.OnLaserPathAnimationComplete -> {
                _state.update { it.copy(laserPenPath = null) }
            }

            is WhiteboardEvent.OnColorPaletteIconClick -> {
                _state.update {
                    it.copy(
                        isColorSelectionDialogOpen = true,
                        selectedColorPaletteType = event.colorPaletteType
                    )
                }
            }

            WhiteboardEvent.ColorSelectionDialogDismiss -> {
                _state.update { it.copy(isColorSelectionDialogOpen = false) }
            }

            is WhiteboardEvent.OnColorSelected -> {
                val state = state.value
                val color = event.color
                val updatedColors = addColorToPreferredList(
                    newColor = color,
                    colors = when (state.selectedColorPaletteType) {
                        ColorPaletteType.CANVAS -> state.preferredCanvasColors
                        ColorPaletteType.STROKE -> state.preferredStrokeColors
                        ColorPaletteType.MARKER -> state.preferredStrokeColors
                        ColorPaletteType.FILL -> state.preferredFillColors
                    }
                )
                when (state.selectedColorPaletteType) {
                    ColorPaletteType.CANVAS -> {
                        _state.update { it.copy(canvasColor = color) }
                        upsertWhiteboard()
                    }

                    ColorPaletteType.STROKE -> {
                        _state.update { it.copy(strokeColor = color) }
                    }

                    ColorPaletteType.FILL -> {
                        _state.update { it.copy(fillColor = color) }
                    }

                    ColorPaletteType.MARKER -> {
                        savePreferredColors(updatedColors, ColorPaletteType.STROKE)
                        onEvent(WhiteboardEvent.StrokeColorChange(color, true))
                        return
                    }
                }
                savePreferredColors(updatedColors, state.selectedColorPaletteType)
            }

            is WhiteboardEvent.OnColorDeleted -> {
                val state = state.value
                val colors = when (event.colorPaletteType) {
                    ColorPaletteType.CANVAS -> state.preferredCanvasColors
                    ColorPaletteType.STROKE -> state.preferredStrokeColors
                    ColorPaletteType.MARKER -> state.preferredStrokeColors
                    ColorPaletteType.FILL -> state.preferredFillColors
                }
                if (colors.size == 1) {
                    onEvent(WhiteboardEvent.SetColorDeletionMode(false))
                    return
                }
                savePreferredColors(colors.minus(event.color), event.colorPaletteType)
                upsertWhiteboard()
            }

            is WhiteboardEvent.SetColorDeletionMode -> {
                _state.update { it.copy(
                    colorDeletionMode = event.on
                ) }
            }

            is WhiteboardEvent.OnCardClose -> {
                _state.update { it.copy(
                    isColorPickerOpen = false,
                    isColorSelectionDialogOpen = false
                ) }
            }

            is WhiteboardEvent.Undo -> {
                var pointer: Int? = state.value.updatePointer ?: return
                if (!canUndo) return
                val update = state.value.updates[pointer!!]
                onUpdate(update.undo(), true)
                deleteUpdate(update)
                pointer -= 1
                if (pointer < 0)
                    pointer = null
                _state.update { it.copy(updatePointer = pointer, undoArray = it.undoArray.plus(update)) }
                upsertWhiteboard(pointer)
            }

            is WhiteboardEvent.Redo -> {
                val pointer: Int = state.value.updatePointer ?: -1
                if (pointer > state.value.updates.size)
                    return
                if (!canUndo) return
                val lastUpdate = state.value.undoArray.lastOrNull() ?: return
                onUpdate(lastUpdate, false)
                insertUpdate(lastUpdate)
                _state.update { it.copy(updatePointer = pointer + 1, undoArray = it.undoArray.dropLast(1)) }
                upsertWhiteboard(pointer + 1)
            }

            is WhiteboardEvent.SetCaptureController -> {
                _state.update { it.copy(captureController = event.captureController) }
            }

            is WhiteboardEvent.SavePicture -> {
                println("Saving picture")
                val captureController = state.value.captureController ?: return
                capture(
                    event.scope,
                    captureController,
                    contextProvider,
                    state.value.whiteboardName,
                    false,
                    null
                ) {}
            }

            is WhiteboardEvent.SaveMiniature -> {
                if (!updateMiniature) return
                updateMiniature = false
                val captureController = state.value.captureController ?: return
                capture(
                    event.scope,
                    captureController,
                    contextProvider,
                    state.value.whiteboardName,
                    true,
                    _state.value.miniatureSrc
                ) {
                    file: File ->
                    _state.update { it.copy(miniatureSrc = file.path) }
                    upsertWhiteboard(miniatureSrc = file.path)
                }
            }

            is WhiteboardEvent.OnStrokeWidthSliderClose -> {
                _state.update { it.copy(isStrokeWidthSliderOpen = false) }
            }

            is WhiteboardEvent.StrokeWidthButtonClicked -> {
                val open = state.value.activeStrokeWidthButton == event.strokeNum && !state.value.isStrokeWidthSliderOpen
                _state.update { it.copy(
                    activeStrokeWidthButton = event.strokeNum,
                    isStrokeWidthSliderOpen = open
                ) }
            }

            is WhiteboardEvent.StrokeSliderValueChange -> {
                val rounded = event.strokeWidth.roundTo(0.1f)
                val current = _state.value.strokeWidthList[_state.value.activeStrokeWidthButton]

                if (rounded != current) {
                    _state.update {
                        it.copy(strokeWidthList = it.strokeWidthList.toMutableList().apply { this[it.activeStrokeWidthButton] = rounded })
                    }
                    upsertWhiteboard()
                }
            }

            is WhiteboardEvent.OnPalettePicked -> {
                val newColorList = event.palette.colorList.minus(event.palette.background)
                _state.update { it.copy(
                    canvasColor = event.palette.background,
                    strokeColor = newColorList[it.selectedMarker],
                    markerColors = newColorList,
                    preferredStrokeColors = newColorList,
                    preferredFillColors = newColorList
                ) }
                if (_state.value.updates.isNotEmpty()) {
                    updateMiniature = true
                    updateMiniatureTask.start(10)
                }
                else {
                    upsertWhiteboard()
                }
            }
        }
    }

    private fun onUpdate(update: Update, undo: Boolean? = null, skipMiniature: Boolean = false) {
        println("Updating: $update")
        val add: Boolean
        val path: DrawnPath
        when (update) {
            is Update.AddPath -> {
                add = true
                path = update.path
            }
            is Update.Erase -> {
                add = true
                path = update.path
                path.strokeColor = state.value.canvasColor
            }
            is Update.RemovePath -> {
                add = false
                path = update.path
            }
            is Update.RemoveErase -> {
                add = false
                path = update.path
            }
        }

        if (undo == null)
            _state.value.undoArray.forEach { deleteUpdate(it) }

        _state.update {
            it.copy(
                updates =
                    if (undo == true)
                        it.updates.dropLast(1)
                    else
                        it.updates.plus(update),
                undoArray =
                    if (undo == null)
                        emptyList()
                    else
                        it.undoArray,
                updatePointer = if (undo == null) it.updates.size else it.updatePointer, // it.updates.size is size - 1
                paths =
                    if (add) {
                        if (it.paths.findLast { it.id == path.id } == null)
                            it.paths.plus(path)
                        else
                            it.paths
                    }
                    else
                        it.paths.filterNot { it.id == path.id || it.id == null }
            )
        }
        if (skipMiniature)
            return
        updateMiniature = true
        updateMiniatureTask.start(4000)
    }

    private fun insertUpdate(update: Update) {
        viewModelScope.launch {
            val updateId = updateRepository.upsertUpdate(update)
            update.id = updateId
        }
    }

    private fun deleteUpdate(update: Update) {
        viewModelScope.launch {
            updateRepository.deleteUpdate(update)
        }
    }

    private fun initializeUpdates(whiteboardId: Long) {
            viewModelScope.launch {
                updateRepository.getWhiteboardUpdates(whiteboardId)
                    .take(1)
                    .collectLatest { updates ->
                        updates.forEach {
                            if (it is Update.AddPath && it.path.drawingTool == DrawingTool.ARROW) {
                                val measure = PathMeasure()
                                measure.setPath(it.path.path, false)
                                val startPos = measure.getPosition(0f)
                                val endPos = measure.getPosition(measure.length)

                                val drawnPath = DrawnPath(
                                    it.path.id,
                                    createArrowPath(startPos, endPos, it.path.strokeWidth),
                                    DrawingTool.ARROW,
                                    it.path.strokeWidth,
                                    it.path.strokeColor,
                                    it.path.fillColor,
                                    it.path.opacity
                                )

                                onUpdate(Update.AddPath(drawnPath, it.id, it.whiteboardId), skipMiniature = true)
                            }
                            else
                                onUpdate(it, skipMiniature = true)
                        }
                    }
            }
    }

    // Creates a new path and update with correct ids
    private fun insertPathAndUpdate(path: DrawnPath, update: Update) {
        viewModelScope.launch {
            val pathId = pathRepository.upsertPath(path)
            path.id = pathId
            val update = update.copyWithPath(path)
            insertUpdate(update)
            onUpdate(update)
        }
    }

    private fun deletePaths(paths: List<DrawnPath>) {
        viewModelScope.launch {
            paths.forEach { path ->
                pathRepository.deletePath(path)
            }
        }
    }

    private fun initializeWhiteboardById(whiteboardId: Long) {
        viewModelScope.launch {
            val whiteboard = whiteboardRepository.getWhiteboardById(whiteboardId)
            whiteboard?.let {
                _state.update {
                    it.copy(
                        whiteboardName = whiteboard.name,
                        canvasColor = whiteboard.palette.background,
                        preferredStrokeColors = whiteboard.palette.colorList.minus(whiteboard.palette.background),
                        preferredFillColors = whiteboard.palette.colorList.minus(whiteboard.palette.background),
                        markerColors = whiteboard.markerColors,
                        strokeColor = whiteboard.palette.foreground,
                        strokeWidthList = whiteboard.strokeWidths,
                        activeStrokeWidthButton = whiteboard.activeStrokeWidthButton,
                        opacity = whiteboard.opacity,
                        fillColor = whiteboard.fillColor,
                        updatePointer = whiteboard.pointer,
                        miniatureSrc = whiteboard.miniatureSrc
                    )
                }
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun upsertWhiteboard(pointer: Int? = state.value.updatePointer, miniatureSrc: String? = state.value.miniatureSrc) {
        GlobalScope.launch(Dispatchers.IO) {
            val now = Clock.System.now()
            val oldWhiteboardDate = if (updatedWhiteboardId.value == null) now else whiteboardRepository.getWhiteboardById(updatedWhiteboardId.value!!)?.createTime ?: now

            val whiteboard = Whiteboard(
                name = _state.value.whiteboardName,
                createTime = oldWhiteboardDate,
                lastModified = now,
                palette = _state.value.palette,
                markerColors = _state.value.markerColors,
                strokeWidths = _state.value.strokeWidthList,
                activeStrokeWidthButton = _state.value.activeStrokeWidthButton,
                opacity = _state.value.opacity,
                fillColor = _state.value.fillColor,
                id = updatedWhiteboardId.value,
                pointer = pointer,
                miniatureSrc = miniatureSrc
            )
            val newId = whiteboardRepository.upsertWhiteboard(whiteboard)
            updatedWhiteboardId.value = newId
        }
    }

    private fun savePreferredColors(
        colors: List<Color>,
        colorPaletteType: ColorPaletteType
    ) {
        viewModelScope.launch {
            settingsRepository.savePreferredColors(colors, colorPaletteType)
        }
        when (colorPaletteType) {
            ColorPaletteType.MARKER -> _state.update { it.copy(markerColors = colors) }
            ColorPaletteType.STROKE -> _state.update { it.copy(preferredStrokeColors = colors) }
            ColorPaletteType.FILL -> _state.update { it.copy(preferredFillColors = colors) }
            ColorPaletteType.CANVAS -> {
                viewModelScope.launch {
                    settingsRepository.savePreferredColors(colors, colorPaletteType)
                }
            }
        }
    }

    private fun updateContinuingOffsets(continuingOffset: Offset) {

        val startOffset = state.value.startingOffset

        val updatedPath: Path? = when (state.value.selectedDrawingTool) {
            DrawingTool.PEN, DrawingTool.HIGHLIGHTER, DrawingTool.LASER_PEN, DrawingTool.ERASER -> {
                createFreehandPath(start = startOffset, continuingOffset = continuingOffset)
            }

            DrawingTool.DELETER -> {
                updatePathsToBeDeleted(start = startOffset, continuingOffset = continuingOffset)
                for (path in state.value.pathsToBeDeleted) {
                    if (path.strokeColor == state.value.canvasColor)
                        continue
                    val update = Update.RemovePath(path, whiteboardId = updatedWhiteboardId.value)
                    insertUpdate(update)
                    onUpdate(update)
                }
                _state.update { it.copy(pathsToBeDeleted = emptyList()) }
                createEraserPath(continuingOffset = continuingOffset)
            }

            DrawingTool.LINE -> {
                createLinePath(start = startOffset, continuingOffset = continuingOffset)
            }

            DrawingTool.ARROW -> {
                createArrowPath(start = startOffset, continuingOffset = continuingOffset)
            }

            DrawingTool.RECTANGLE -> {
                createRectanglePath(start = startOffset, continuingOffset = continuingOffset)
            }

            DrawingTool.CIRCLE -> {
                createCirclePath(start = startOffset, continuingOffset = continuingOffset)
            }

            DrawingTool.TRIANGLE -> {
                createTrianglePath(start = startOffset, continuingOffset = continuingOffset)
            }
        }

        updatedWhiteboardId.value?.let { id ->
            var eraserSize = state.value.strokeWidth
            eraserSize = max(eraserSize * 2f, eraserSize + 20)
            _state.update {
                it.copy(
                    currentPath = updatedPath?.let { path ->
                        DrawnPath(
                            path = path,
                            drawingTool = state.value.selectedDrawingTool,
                            strokeColor =
                                if (state.value.selectedDrawingTool == DrawingTool.ERASER)
                                    state.value.canvasColor
                                else
                                    state.value.strokeColor,
                            fillColor = state.value.fillColor,
                            opacity =
                                if (state.value.selectedDrawingTool == DrawingTool.HIGHLIGHTER)
                                    40f
                                else
                                    state.value.opacity,
                            strokeWidth =
                                if (state.value.selectedDrawingTool == DrawingTool.ERASER || state.value.selectedDrawingTool == DrawingTool.HIGHLIGHTER)
                                    eraserSize
                                else
                                    state.value.strokeWidth
                        )
                    }
                )
            }
        }
    }

    private fun createEraserPath(continuingOffset: Offset): Path {
        return Path().apply {
            addOval(Rect(center = continuingOffset, radius = 5f))
        }
    }

    private fun createFreehandPath(start: Offset, continuingOffset: Offset): Path {
        val existingPath = state.value.currentPath?.path ?: Path().apply {
            moveTo(start.x, start.y)
        }
        return Path().apply {
            addPath(existingPath)
            lineTo(continuingOffset.x, continuingOffset.y)
        }
    }

    private fun createLinePath(start: Offset, continuingOffset: Offset): Path {
        return Path().apply {
            moveTo(start.x, start.y)
            lineTo(continuingOffset.x, continuingOffset.y)
        }
    }

    private fun createArrowPath(start: Offset, continuingOffset: Offset, strokeWidth: Float = state.value.strokeWidth): Path {
        val arrowHeadAngle = 30.0
        val arrowHeadLength = 70f * (max(5f, strokeWidth) / 8f)

        return Path().apply {
            // Main line
            moveTo(start.x, start.y)
            lineTo(continuingOffset.x, continuingOffset.y)

            // Angle of the arrow in radians
            val angle = atan2(
                continuingOffset.y - start.y,
                continuingOffset.x - start.x
            )

            // Offset tip backward by half line thickness
            val tipOffsetX = strokeWidth * 0.5f * cos(angle)
            val tipOffsetY = strokeWidth * 0.5f * sin(angle)
            val adjustedTipX = continuingOffset.x - tipOffsetX
            val adjustedTipY = continuingOffset.y - tipOffsetY

            // Arrowhead side angles
            val angle1 = angle - Math.toRadians(arrowHeadAngle).toFloat()
            val angle2 = angle + Math.toRadians(arrowHeadAngle).toFloat()

            // Left side point
            val x1 = adjustedTipX - arrowHeadLength * cos(angle1)
            val y1 = adjustedTipY - arrowHeadLength * sin(angle1)

            // Right side point
            val x2 = adjustedTipX - arrowHeadLength * cos(angle2)
            val y2 = adjustedTipY - arrowHeadLength * sin(angle2)

            // Draw arrowhead lines meeting at adjusted tip
            moveTo(adjustedTipX, adjustedTipY)
            lineTo(x1, y1)

            moveTo(adjustedTipX, adjustedTipY)
            lineTo(x2, y2)
        }
    }

    private fun createRectanglePath(start: Offset, continuingOffset: Offset): Path {
        val topLeft = Offset(min(start.x, continuingOffset.x), min(start.y, continuingOffset.y))
        val bottomRight = Offset(max(start.x, continuingOffset.x), max(start.y, continuingOffset.y))
        return Path().apply {
            addRect(Rect(topLeft, bottomRight))
        }
    }

    private fun createCirclePath(start: Offset, continuingOffset: Offset): Path {
        val width = continuingOffset.x - start.x
        val height = continuingOffset.y - start.y
        return Path().apply {
            addOval(Rect(offset = start, size = Size(width = width, height = height)))
        }
    }

    private fun createTrianglePath(start: Offset, continuingOffset: Offset): Path {
        val height = continuingOffset.y - start.y
        val baseWidth = continuingOffset.x - start.x
        val remainingVertex = Offset(x = start.x - baseWidth, y = start.y + height)

        return Path().apply {
            moveTo(start.x, start.y)
            lineTo(continuingOffset.x, continuingOffset.y)
            lineTo(remainingVertex.x, remainingVertex.y)
            close()
        }
    }

    private fun updatePathsToBeDeleted(start: Offset, continuingOffset: Offset) {
        val pathsToBeDeleted = state.value.pathsToBeDeleted.toMutableList()
        state.value.paths.forEach { drawnPath ->
            val bounds = drawnPath.path.getBounds()
            if (bounds.contains(start) || bounds.contains(continuingOffset)) {
                if (!pathsToBeDeleted.contains(drawnPath)) {
                    pathsToBeDeleted.add(drawnPath)
                }
            }
        }
        _state.update { it.copy(pathsToBeDeleted = pathsToBeDeleted) }
    }

    private fun addColorToPreferredList(
        newColor: Color,
        colors: List<Color>
    ): List<Color> {
        return colors.filter { it != newColor }.take(n = 23) + listOf(newColor)
    }

    private fun initializeWhiteboardName(translatePolish: Boolean): String {
        fun getDayOfWeekInPolish(weekDay: DayOfWeek): String {
            return when(weekDay) {
                java.time.DayOfWeek.MONDAY -> "Poniedziałek"
                java.time.DayOfWeek.TUESDAY -> "Wtorek"
                java.time.DayOfWeek.WEDNESDAY -> "Środa"
                java.time.DayOfWeek.THURSDAY -> "Czwartek"
                java.time.DayOfWeek.FRIDAY -> "Piątek"
                java.time.DayOfWeek.SATURDAY -> "Sobota"
                java.time.DayOfWeek.SUNDAY -> "Niedziela"
            }
        }

        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        return getDayOfWeekInPolish(today.dayOfWeek) + " " + today.formatDate()
    }
}



