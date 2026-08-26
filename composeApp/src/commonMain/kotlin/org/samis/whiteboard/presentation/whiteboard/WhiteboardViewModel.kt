package org.samis.whiteboard.presentation.whiteboard

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.unit.IntSize
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.samis.whiteboard.data.mapper.toPaletteEntity
import org.samis.whiteboard.domain.model.ColorPaletteType
import org.samis.whiteboard.domain.model.DrawingTool
import org.samis.whiteboard.domain.model.DrawnPath
import org.samis.whiteboard.domain.model.Update
import org.samis.whiteboard.domain.model.Whiteboard
import org.samis.whiteboard.domain.repository.PaletteRepository
import org.samis.whiteboard.domain.repository.PathRepository
import org.samis.whiteboard.domain.repository.SettingsRepository
import org.samis.whiteboard.domain.repository.UpdateRepository
import org.samis.whiteboard.domain.repository.WhiteboardRepository
import org.samis.whiteboard.presentation.navigation.Routes
import org.samis.whiteboard.presentation.theme.Palettes
import org.samis.whiteboard.presentation.util.AppScope
import org.samis.whiteboard.presentation.util.DelayedTask
import org.samis.whiteboard.presentation.util.DrawingToolVisibility
import org.samis.whiteboard.presentation.util.IContextProvider
import org.samis.whiteboard.presentation.util.Palette
import org.samis.whiteboard.presentation.util.capture
import org.samis.whiteboard.presentation.util.findPathsAt
import org.samis.whiteboard.presentation.util.formatDate
import org.samis.whiteboard.presentation.util.minusLast
import org.samis.whiteboard.presentation.util.roundTo
import java.io.File
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

class WhiteboardViewModel(
    private val pathRepository: PathRepository,
    private val updateRepository: UpdateRepository,
    private val whiteboardRepository: WhiteboardRepository,
    private val settingsRepository: SettingsRepository,
    private val paletteRepository: PaletteRepository,
    savedStateHandle: SavedStateHandle,
    private val contextProvider: IContextProvider
) : ViewModel() {

    private val smoothPoints = false

    private val whiteboardId = savedStateHandle.toRoute<Routes.WhiteboardScreen>().whiteboardId
    private var canUndo = true
    private var isFirstPath = true
    private val currentPathPoints = mutableListOf<Offset>()
    private var pendingInitCanvasSize: IntSize? = null

    private var updatedWhiteboardId = MutableStateFlow(whiteboardId)
    private var updateMiniature = false
    private val updateMiniatureTask = DelayedTask<WhiteboardState>(AppScope.scope) {
        whiteboardState -> onEvent(WhiteboardEvent.SaveMiniature(viewModelScope, whiteboardState))
    }

    private val _state = MutableStateFlow(WhiteboardState())
    val state = combine(
        _state,
        settingsRepository.getPreferredCanvasColors(),
        settingsRepository.getDrawingToolVisibility(),
        settingsRepository.getStylusInput(),
        paletteRepository.getAllPalettes(),
        settingsRepository.getShowOpacitySlider()
    ){ flows ->
        val state = flows[0] as WhiteboardState
        canUndo = true
        state.copy(
            preferredCanvasColors = flows[1] as List<Color>,
            drawingToolVisibility = flows[2] as DrawingToolVisibility,
            stylusInput = flows[3] as Boolean,
            paletteList = flows[4] as List<Palette>,
            showOpacitySlider = flows[5] as Boolean
        )
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = WhiteboardState()
    )

    init {
        whiteboardId?.let { id ->
            initializeWhiteboardById(id)
            initializeUpdates(id)
        }
        if (whiteboardId == null)
            initializeDefaultPalette()
    }

    fun onEvent(event: WhiteboardEvent) {
        when (event) {
            is WhiteboardEvent.StartDrawing -> {
                if (isFirstPath && _state.value.selectedDrawingTool != DrawingTool.LASER_PEN) {
                    if (whiteboardId == null)
                        _state.update { it.copy(whiteboardName = initializeWhiteboardName(translatePolish = true)) }

                    viewModelScope.launch {
                        _state.first()
                        upsertWhiteboard()
                    }
                    isFirstPath = false
                }
                val logicalOffset = (event.offset - _state.value.canvasOffset) / _state.value.canvasScale
                currentPathPoints.clear()
                currentPathPoints.add(logicalOffset)
                _state.update { it.copy(startingOffset = logicalOffset, previousOffset = null) }
                if (_state.value.selectedDrawingTool != DrawingTool.LASER_PEN)
                    updateMiniature = false
            }

            is WhiteboardEvent.ContinueDrawing -> {
                val logicalOffset = (event.continuingOffset - _state.value.canvasOffset) / _state.value.canvasScale
                currentPathPoints.add(logicalOffset)
                updateContinuingOffsets(logicalOffset)
                _state.update { it.copy(previousOffset = logicalOffset) }
            }

            WhiteboardEvent.FinishDrawing -> {
                if (smoothPoints && _state.value.selectedDrawingTool.isSmoothable()) {
                    val simplified = simplifyPath(currentPathPoints, 1.8f)
                    currentPathPoints.clear()
                    currentPathPoints.addAll(simplified)
                    val finalPath = pathFromPointList(simplified)
                    _state.update { it.copy(currentPath = _state.value.currentPath?.copy(path = finalPath)) }
                }
                _state.value.currentPath?.let { drawnPath ->
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
                            _state.update { it.copy(selectedDrawingTool = DrawingTool.PEN, previousOffset = null) }
                        }

                        DrawingTool.LASER_PEN -> {
                            _state.update { it.copy(laserPenPath = drawnPath, previousOffset = null) }
                        }

                        else -> {
                            insertPathAndUpdate(
                                drawnPath,
                                Update.AddPath(drawnPath, whiteboardId = updatedWhiteboardId.value)
                            )
                            _state.update { it.copy(previousOffset = null) }
                        }
                    }
                }
                currentPathPoints.clear()

                _state.update {
                    it.copy(
                        // removes flickering
                        paths =
                            if (it.selectedDrawingTool != DrawingTool.LASER_PEN && it.selectedDrawingTool != DrawingTool.DELETER && it.currentPath != null)
                                it.paths.plus(it.currentPath!!)
                            else it.paths,
                        currentPath = null,
                        pathsToBeDeleted = hashSetOf()
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
                if (whiteboardId != null)
                    upsertWhiteboard()
            }

            is WhiteboardEvent.OpacitySliderValueChange -> {
                _state.update { it.copy(opacity = event.opacity) }
                if (whiteboardId != null)
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

                if (whiteboardId != null) {
                    upsertWhiteboard()
                    updateMiniature = true
                    updateMiniatureTask.start(4000, _state.value.copy())
                }
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

            is WhiteboardEvent.OnDrawingToolButtonClick -> {
                _state.update { it.copy(isDrawingToolDialogOpen = !_state.value.isDrawingToolDialogOpen) }
            }

            is WhiteboardEvent.OnDrawingToolDialogClose -> {
                _state.update { it.copy(isDrawingToolDialogOpen = false) }
            }

            is WhiteboardEvent.OnCommandPaletteIconClick -> {
                _state.update {
                    it.copy(isCommandPaletteOpen = !_state.value.isCommandPaletteOpen)
                }
                if (state.value.paletteList.isEmpty()) {
                    _state.update {
                        it.copy(paletteList = Palettes.defaultPalettes)
                    }
                    viewModelScope.launch {
                        Palettes.defaultPalettes.forEach {
                            paletteRepository.upsertPalette(it)
                        }
                    }
                }
            }

            is WhiteboardEvent.OnCommandPaletteClose -> {
                _state.update {
                    it.copy(isCommandPaletteOpen = false)
                }
            }

            is WhiteboardEvent.OnColorPaletteIconClick -> {
                _state.update {
                    it.copy(
                        isColorSelectionDialogOpen = true,
                        selectedColorPaletteType = event.colorPaletteType
                    )
                }
            }

            is WhiteboardEvent.OnPaletteEditMode -> {
                _state.update { it.copy(isPaletteEditMode = !it.isPaletteEditMode) }
            }

            is WhiteboardEvent.OnPaletteAdded -> {
                viewModelScope.launch {
                    paletteRepository.upsertPalette(event.palette)
                    settingsRepository.saveLastPalette(event.palette)
                }
            }

            is WhiteboardEvent.OnPaletteRemoved -> {
                val palette = state.value.paletteList.find { it.toPaletteEntity() == event.palette.toPaletteEntity() }
                viewModelScope.launch {
                    paletteRepository.deletePalette(palette ?: state.value.paletteToDelete!!)
                }
                _state.update { it.copy(
                    showRemovePaletteDialog = false,
                    paletteToDelete = null
                ) }
            }

            is WhiteboardEvent.ShowRemovePaletteDialog -> {
                _state.update { it.copy(
                    showRemovePaletteDialog = true,
                    paletteToDelete = event.palette
                ) }
            }

            is WhiteboardEvent.HideRemovePaletteDialog -> {
                _state.update { it.copy(showRemovePaletteDialog = false) }
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

                        updateMiniature = true
                        updateMiniatureTask.start(4000, _state.value.copy())
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
                    onEvent(WhiteboardEvent.SetColorDeletionMode(false, event.colorPaletteType))
                    return
                }
                savePreferredColors(colors.minus(event.color), event.colorPaletteType)
                if (whiteboardId != null)
                    upsertWhiteboard()
            }

            is WhiteboardEvent.SetColorDeletionMode -> {
                _state.update {
                    if (event.colorType == ColorPaletteType.CANVAS) {
                        it.copy(canvasColorDeletionMode = event.on)
                    } else {
                        it.copy(markerColorDeletionMode = event.on)
                    }
                }
            }

            is WhiteboardEvent.OnTitleChange -> {
                _state.update { it.copy(whiteboardName = event.title) }
                if (whiteboardId != null)
                    upsertWhiteboard()
            }

            is WhiteboardEvent.OnCardClose -> {
                _state.update { it.copy(
                    isColorPickerOpen = false,
                    isColorSelectionDialogOpen = false
                ) }
            }

            is WhiteboardEvent.Undo -> {
                var pointer: Int? = _state.value.updatePointer ?: return
                if (!canUndo) return
                val update = _state.value.updates[pointer!!]
                onUpdate(update.undo(), true)
                deleteUpdate(update)
                pointer -= 1
                if (pointer < 0)
                    pointer = null
                _state.update { it.copy(updatePointer = pointer, undoArray = it.undoArray.plus(update)) }
                upsertWhiteboard(pointer)
            }

            is WhiteboardEvent.Redo -> {
                val pointer: Int = _state.value.updatePointer ?: -1
                if (pointer > _state.value.updates.size)
                    return
                if (!canUndo) return
                val lastUpdate = _state.value.undoArray.lastOrNull() ?: return
                onUpdate(lastUpdate, false)
                insertUpdate(lastUpdate)
                _state.update { it.copy(updatePointer = pointer + 1, undoArray = it.undoArray.dropLast(1)) }
                upsertWhiteboard(pointer + 1)
            }

            is WhiteboardEvent.SetCaptureController -> {
                _state.update { it.copy(captureController = event.captureController) }
            }

            is WhiteboardEvent.SavePicture -> {
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
                val whiteboardState = event.stateSnapshot ?: _state.value
                val captureController = whiteboardState.captureController ?: return
                capture(
                    event.scope,
                    captureController,
                    contextProvider,
                    whiteboardState.whiteboardName,
                    true,
                    whiteboardState.miniatureSrc
                ) {
                    file: File ->
                    var newMiniatureSrc = whiteboardState.miniatureSrc
                    if (file.path.isNotEmpty())
                        newMiniatureSrc = file.path
                    _state.update { it.copy(miniatureSrc = newMiniatureSrc) }
                    upsertWhiteboard(miniatureSrc = newMiniatureSrc, whiteboardId = updatedWhiteboardId.value)
                }
            }

            is WhiteboardEvent.OnStrokeWidthSliderClose -> {
                _state.update { it.copy(isStrokeWidthSliderOpen = false) }
            }

            is WhiteboardEvent.CanvasTransformed -> {
                val oldScale = _state.value.canvasScale
                val newScale = (oldScale * event.zoomChange).coerceIn(1f / 12f, 12f)
                val scaleRatio = newScale / oldScale
                val newOffset = event.center - (event.center - _state.value.canvasOffset) * scaleRatio + event.offset
                _state.update { it.copy(canvasOffset = newOffset, canvasScale = newScale) }
                updateMiniature = true
                updateMiniatureTask.start(4000, _state.value.copy())
            }

            is WhiteboardEvent.CanvasSizeChanged -> {
                val newSize = event.size
                val oldSize = _state.value.canvasSize

                if (newSize == IntSize.Zero)
                    return
                if (whiteboardId == null) {
                    _state.update { it.copy(canvasSize = newSize) }
                    return
                }
                if (_state.value.paths.isEmpty()) {
                    pendingInitCanvasSize = newSize
                    return
                }

                applyCanvasSizeChange(oldSize, newSize)
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
                    if (whiteboardId != null)
                        upsertWhiteboard()
                }
            }

            is WhiteboardEvent.OnPalettePicked -> {
                val newColorList = event.palette.colorList.minus(event.palette.background)
                _state.value.updates.forEach {
                    if (it is Update.Erase) {
                        it.path.strokeColor = event.palette.background
                    }
                }
                _state.update { it.copy(
                    canvasColor = event.palette.background,
                    strokeColor = newColorList[it.selectedMarker],
                    markerColors = newColorList,
                    preferredStrokeColors = newColorList,
                    preferredFillColors = newColorList
                ) }
                if (whiteboardId != null) {
                    updateMiniature = true
                    updateMiniatureTask.start(4000, _state.value.copy())
                    upsertWhiteboard()
                }
                viewModelScope.launch {
                    settingsRepository.saveLastPalette(event.palette)
                }
            }

            is WhiteboardEvent.ZoomSliderVisibilityChange -> {
                val zoomSliderOpen = event.visible ?: !_state.value.isZoomSliderOpen
                _state.update { it.copy(isZoomSliderOpen = zoomSliderOpen) }
            }

            is WhiteboardEvent.CanvasZoomChange -> {
                val canvasSize = _state.value.canvasSize
                val screenCenter = Offset(
                    x = canvasSize.width / 2f,
                    y = canvasSize.height / 2f
                )
                onEvent(WhiteboardEvent.CanvasTransformed(
                    center = screenCenter,
                    offset = Offset.Zero,
                    zoomChange = event.zoom / _state.value.canvasScale
                ))
            }
        }
    }

    private fun onUpdate(update: Update, undo: Boolean? = null, skipMiniature: Boolean = false) {
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

        _state.update {
            it.copy(
                updates =
                    if (undo == true)
                        it.updates.dropLast(1)
                    else
                        it.updates.plus(update),
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
        updateMiniatureTask.start(4000, _state.value.copy())
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

    private fun applyCanvasSizeChange(oldSize: IntSize, newSize: IntSize) {
        if (oldSize == newSize) return

        val newOffset = if (oldSize == IntSize.Zero) {
            val savedSize = _state.value.canvasSize
            if (savedSize == IntSize.Zero) {
                _state.value.canvasOffset
            } else {
                _state.value.canvasOffset + Offset(
                    x = (newSize.width - savedSize.width) / 2f,
                    y = (newSize.height - savedSize.height) / 2f
                )
            }
        } else {
            _state.value.canvasOffset + Offset(
                x = (newSize.width - oldSize.width) / 2f,
                y = (newSize.height - oldSize.height) / 2f
            )
        }

        _state.update { it.copy(canvasSize = newSize, canvasOffset = newOffset) }
        updateMiniature = true
        updateMiniatureTask.start(4000, _state.value.copy())
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
                        }.also {
                            val pending = pendingInitCanvasSize
                            pendingInitCanvasSize = null
                            if (pending != null && pending != IntSize.Zero)
                                applyCanvasSizeChange(oldSize = IntSize.Zero, newSize = pending)
                        }
                    }
            }
    }

    // Creates a new path and update with correct ids
    private fun insertPathAndUpdate(path: DrawnPath, update: Update) {
        viewModelScope.launch {
            val currentPath: DrawnPath? = _state.value.currentPath
            val pathId = pathRepository.upsertPath(path, currentPathPoints)
            path.id = pathId
            val update = update.copyWithPath(path)
            insertUpdate(update)
            onUpdate(update)
            if (currentPath != null && currentPath.id == null)
                _state.update { it.copy(paths = _state.value.paths.minusLast(currentPath)) }
        }
    }

    private fun initializeDefaultPalette() {
        viewModelScope.launch {
            val palette = settingsRepository.getLastPalette().first()

            val newColorList = palette.colorList.minus(palette.background)
            _state.update { it.copy(
                canvasColor = palette.background,
                strokeColor = newColorList[it.selectedMarker],
                markerColors = newColorList,
                preferredStrokeColors = newColorList,
                preferredFillColors = newColorList
            ) }
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
                        miniatureSrc = whiteboard.miniatureSrc,
                        canvasSize = whiteboard.canvasSize,
                        canvasOffset = whiteboard.canvasOffset,
                        canvasScale = whiteboard.canvasScale
                    )
                }
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun upsertWhiteboard(
        pointer: Int? = _state.value.updatePointer,
        snapshot: WhiteboardState? = null,
        miniatureSrc: String? = _state.value.miniatureSrc,
        whiteboardId: Long? = updatedWhiteboardId.value) {
        val snapshot = snapshot ?: _state.value.copy(updatePointer = pointer, miniatureSrc = miniatureSrc)
        GlobalScope.launch(Dispatchers.IO) {
            val now = Clock.System.now()
            val oldWhiteboardDate = if (whiteboardId == null) now else whiteboardRepository.getWhiteboardById(updatedWhiteboardId.value!!)?.createTime ?: now

            val whiteboard = Whiteboard(
                name = snapshot.whiteboardName,
                createTime = oldWhiteboardDate,
                lastModified = now,
                palette = snapshot.palette,
                markerColors = snapshot.markerColors,
                strokeWidths = snapshot.strokeWidthList,
                activeStrokeWidthButton = snapshot.activeStrokeWidthButton,
                opacity = snapshot.opacity,
                fillColor = snapshot.fillColor,
                id = whiteboardId,
                pointer = pointer,
                miniatureSrc = miniatureSrc,
                canvasSize = snapshot.canvasSize,
                canvasOffset = snapshot.canvasOffset,
                canvasScale = snapshot.canvasScale
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

    override fun onCleared() {
        AppScope.scope.launch {
            _state.value.undoArray.forEach {
                updateRepository.deleteUpdate(it)
                val path = when (it) {
                    is Update.AddPath -> it.path
                    is Update.RemovePath -> it.path
                    is Update.RemoveErase -> it.path
                    is Update.Erase -> it.path
                }
                pathRepository.deletePath(path)
            }
        }
        super.onCleared()
    }

    private fun updateContinuingOffsets(continuingOffset: Offset) {

        val startOffset = state.value.startingOffset

        val updatedPath: Path? = when (state.value.selectedDrawingTool) {
            DrawingTool.PEN, DrawingTool.HIGHLIGHTER, DrawingTool.DASHER, DrawingTool.LASER_PEN, DrawingTool.ERASER -> {
                createFreehandPath(start = startOffset, continuingOffset = continuingOffset)
            }

            DrawingTool.DELETER -> {
                updatePathsToBeDeleted(
                    start = startOffset,
                    previousOffset = state.value.previousOffset,
                    continuingOffset = continuingOffset
                )
                for (path in _state.value.pathsToBeDeleted) {
                    if (path.drawingTool == DrawingTool.ERASER)
                        continue
                    val update = Update.RemovePath(path, whiteboardId = updatedWhiteboardId.value)
                    insertUpdate(update)
                    onUpdate(update)
                }
                _state.update { it.copy(pathsToBeDeleted = hashSetOf()) }
                createDeleterPath(continuingOffset = continuingOffset)
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

            DrawingTool.CANVAS_PANNER -> {
                println("Canvas Panner | Marquee should not create any paths")
                null
            }
        }

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
                            when (state.value.selectedDrawingTool) {
                                DrawingTool.ERASER -> 100f
                                DrawingTool.HIGHLIGHTER -> 40f
                                DrawingTool.DASHER -> 50f
                                else -> state.value.opacity
                            },
                        strokeWidth =
                            when (state.value.selectedDrawingTool) {
                                DrawingTool.ERASER, DrawingTool.HIGHLIGHTER -> eraserSize
                                DrawingTool.DELETER -> 5f
                                else -> _state.value.strokeWidth
                            }
                    )
                }
            )
        }
    }

    private fun createDeleterPath(continuingOffset: Offset): Path {
        return Path().apply {
            addOval(Rect(center = continuingOffset, radius = 5f))
        }
    }

    private fun createFreehandPath(start: Offset, continuingOffset: Offset): Path {
        val existingPath = state.value.currentPath?.path ?: Path().apply {
            moveTo(start.x, start.y)
        }

        val previousOffset = state.value.previousOffset ?: start
        val mid = Offset(
            x = (previousOffset.x + continuingOffset.x) / 2f,
            y = (previousOffset.y + continuingOffset.y) / 2f
        )
        val distance = (continuingOffset - previousOffset).getDistance()
        val distanceToMid = (mid - previousOffset).getDistance()

        return Path().apply {
            addPath(existingPath)
            if (state.value.previousOffset == null || distance < 1f || distanceToMid < 2f)
                lineTo(continuingOffset.x, continuingOffset.y)
            else
                quadraticBezierTo(
                    x1 = previousOffset.x,
                    y1 = previousOffset.y,
                    x2 = mid.x,
                    y2 = mid.y
                )
        }
    }

    fun pathFromPointList(points: List<Offset>): Path {
        if (points.isEmpty()) return Path()
        return Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size - 1) {
                val mid = Offset(
                    x = (points[i].x + points[i + 1].x) / 2f,
                    y = (points[i].y + points[i + 1].y) / 2f
                )
                quadraticBezierTo(points[i].x, points[i].y, mid.x, mid.y)
            }

            val lastMid = Offset(
                x = (points[points.size - 2].x + points.last().x) / 2f,
                y = (points[points.size - 2].y + points.last().y) / 2f
            )
            if ((points.last() - lastMid).getDistance() >= 1f)
                lineTo(points.last().x, points.last().y)
        }
    }

    fun simplifyPath(points: List<Offset>, epsilon: Float): List<Offset> {
        if (points.size < 3) return points

        val first = points.first()
        val last = points.last()
        var maxDistance = 0f
        var maxIndex = 0

        for (i in 1 until points.size - 1) {
            val distance = perpendicularDistance(points[i], first, last)
            if (distance > maxDistance) {
                maxDistance = distance
                maxIndex = i
            }
        }

        return if (maxDistance > epsilon) {
            val left = simplifyPath(points.subList(0, maxIndex + 1), epsilon)
            val right = simplifyPath(points.subList(maxIndex, points.size), epsilon)
            left.dropLast(1) + right
        } else {
            listOf(first, last)
        }
    }

    private fun perpendicularDistance(point: Offset, lineStart: Offset, lineEnd: Offset): Float {
        val dx = lineEnd.x - lineStart.x
        val dy = lineEnd.y - lineStart.y
        val magnitude = sqrt(dx * dx + dy * dy)
        if (magnitude == 0f) return (point - lineStart).getDistance()
        return abs(dy * point.x - dx * point.y + lineEnd.x * lineStart.y - lineEnd.y * lineStart.x) / magnitude
    }

    private fun createLinePath(start: Offset, continuingOffset: Offset): Path {
        return Path().apply {
            moveTo(start.x, start.y)
            lineTo(continuingOffset.x, continuingOffset.y)
        }
    }

    private fun createArrowPath(start: Offset, continuingOffset: Offset, strokeWidth: Float = state.value.strokeWidth): Path {
        val arrowHeadAngle = 30.0
        val length = (continuingOffset - start).getDistance()
        val arrowHeadLength = 100f * (max(5f, strokeWidth) / 8f) * (length / 400f).coerceIn(0.3f, 1f)

        return Path().apply {
            moveTo(start.x, start.y)
            lineTo(continuingOffset.x, continuingOffset.y)

            val angle = atan2(
                continuingOffset.y - start.y,
                continuingOffset.x - start.x
            )

            // Arrowhead side angles
            val angle1 = angle - Math.toRadians(arrowHeadAngle).toFloat()
            val angle2 = angle + Math.toRadians(arrowHeadAngle).toFloat()

            // Left side point
            val x1 = continuingOffset.x - arrowHeadLength * cos(angle1)
            val y1 = continuingOffset.y - arrowHeadLength * sin(angle1)

            // Right side point
            val x2 = continuingOffset.x - arrowHeadLength * cos(angle2)
            val y2 = continuingOffset.y - arrowHeadLength * sin(angle2)

            moveTo(continuingOffset.x, continuingOffset.y)
            lineTo(x1, y1)

            moveTo(continuingOffset.x, continuingOffset.y)
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

    private fun updatePathsToBeDeleted(start: Offset, previousOffset: Offset?, continuingOffset: Offset) {
        val pathsToBeDeleted = _state.value.pathsToBeDeleted.toHashSet()
        val from = previousOffset ?: start
        val distance = continuingOffset - from
        val segmentDistance = (continuingOffset - from).getDistance()
        val steps = maxOf(1f, segmentDistance.toInt() / 5f).toInt()

        for (i in 0..steps) {
            val fraction = i / steps.toFloat()
            val position = from + distance * fraction
            pathsToBeDeleted += findPathsAt(
                touchPoint = position,
                drawnPaths = _state.value.paths,
                rejectedPaths = pathsToBeDeleted,
                canvasOffset = _state.value.canvasOffset,
                canvasScale = _state.value.canvasScale
            )
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
        fun getDayOfWeek(weekDay: DayOfWeek): String {
            return when(weekDay) {
                DayOfWeek.MONDAY -> "Monday"
                DayOfWeek.TUESDAY -> "Tuesday"
                DayOfWeek.WEDNESDAY -> "Wednesday"
                DayOfWeek.THURSDAY -> "Thursday"
                DayOfWeek.FRIDAY -> "Friday"
                DayOfWeek.SATURDAY -> "Saturday"
                DayOfWeek.SUNDAY -> "Sunday"
            }
        }
        fun getDayOfWeekInPolish(weekDay: DayOfWeek): String {
            return when(weekDay) {
                DayOfWeek.MONDAY -> "Poniedziałek"
                DayOfWeek.TUESDAY -> "Wtorek"
                DayOfWeek.WEDNESDAY -> "Środa"
                DayOfWeek.THURSDAY -> "Czwartek"
                DayOfWeek.FRIDAY -> "Piątek"
                DayOfWeek.SATURDAY -> "Sobota"
                DayOfWeek.SUNDAY -> "Niedziela"
            }
        }

        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val weekDay = if (translatePolish) getDayOfWeekInPolish(today.dayOfWeek) else getDayOfWeek(today.dayOfWeek)
        return weekDay + " " + today.formatDate()
    }
}



