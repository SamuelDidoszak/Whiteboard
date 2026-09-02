package org.samis.whiteboard.presentation.whiteboard

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.samis.whiteboard.domain.model.ColorPaletteType
import org.samis.whiteboard.domain.model.DrawingTool
import org.samis.whiteboard.domain.model.DrawnPath
import org.samis.whiteboard.presentation.util.UiType
import org.samis.whiteboard.presentation.util.capturable
import org.samis.whiteboard.presentation.util.detectStylusDragGestures
import org.samis.whiteboard.presentation.util.getUiType
import org.samis.whiteboard.presentation.util.registerBackHandler
import org.samis.whiteboard.presentation.util.rememberCaptureController
import org.samis.whiteboard.presentation.util.rememberPicturePermissionRequester
import org.samis.whiteboard.presentation.util.rememberScreenSizeSize
import org.samis.whiteboard.presentation.whiteboard.component.ColorPickerCard
import org.samis.whiteboard.presentation.whiteboard.component.ColorSelectionDialog
import org.samis.whiteboard.presentation.whiteboard.component.CommandBarHorizontal
import org.samis.whiteboard.presentation.whiteboard.component.CommandBarVertical
import org.samis.whiteboard.presentation.whiteboard.component.CommandPaletteCard
import org.samis.whiteboard.presentation.whiteboard.component.CommandPaletteDrawerContent
import org.samis.whiteboard.presentation.whiteboard.component.DrawingToolBar
import org.samis.whiteboard.presentation.whiteboard.component.DrawingToolDialog
import org.samis.whiteboard.presentation.whiteboard.component.ElevatedIconButton
import org.samis.whiteboard.presentation.whiteboard.component.MarkerColorBar
import org.samis.whiteboard.presentation.whiteboard.component.RemovePaletteDialog
import org.samis.whiteboard.presentation.whiteboard.component.StrokeWidthBar
import org.samis.whiteboard.presentation.whiteboard.component.StrokeWidthSliderCard
import org.samis.whiteboard.presentation.whiteboard.component.ZoomSliderCard
import whiteboard.composeapp.generated.resources.Res
import whiteboard.composeapp.generated.resources.logoWithName

@Composable
fun WhiteboardScreen(
    modifier: Modifier = Modifier,
    state: WhiteboardState,
    navController: NavController,
    onEvent: (WhiteboardEvent) -> Unit,
    onHomeIconClick: () -> Unit
) {

    HideSystemBars()
    rememberCaptureController().also {
        onEvent(WhiteboardEvent.SetCaptureController(it))
    }
    val screenSize = rememberScreenSizeSize()
    val uiType = screenSize.getUiType()

    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val focusManager = LocalFocusManager.current

    val snackbarHostState = remember { SnackbarHostState() }
    val requestPicturePermission = rememberPicturePermissionRequester(state.askedForPermissions)

    LaunchedEffect(drawerState.targetValue) {
        requestPicturePermission(
            false,
            { onEvent(WhiteboardEvent.OnPicturePermissionChanged(true)) },
            { onEvent(WhiteboardEvent.OnPicturePermissionChanged(false)) }
        )
        if (drawerState.targetValue == DrawerValue.Closed) {
            focusManager.clearFocus()
        }
    }

    miniatureSaveHandle(scope, onEvent, navController)

    ColorSelectionDialog(
        isOpen = state.isColorSelectionDialogOpen,
        onColorSelected = { onEvent(WhiteboardEvent.OnColorSelected(it)) },
        onDismiss = { onEvent(WhiteboardEvent.ColorSelectionDialogDismiss) }
    )

    RemovePaletteDialog(
        palette = state.paletteToDelete,
        showDialog = state.showRemovePaletteDialog,
        onDismiss = { onEvent(WhiteboardEvent.HideRemovePaletteDialog) },
        onDelete = { onEvent(WhiteboardEvent.OnPaletteRemoved(state.paletteToDelete!!)) }
    )

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        when (uiType) {
            UiType.COMPACT -> {
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        CommandPaletteDrawerContent(
                            title = state.whiteboardName,
                            canvasColors = state.preferredCanvasColors,
                            selectedCanvasColor = state.canvasColor,
                            palettes = state.paletteList,
                            currentPalette = state.palette,
                            onTitleChange = { onEvent(WhiteboardEvent.OnTitleChange(it)) },
                            onCanvasColorChange = { onEvent(WhiteboardEvent.CanvasColorChange(it)) },
                            onColorPaletteIconClick = {
                                onEvent(WhiteboardEvent.OnColorPaletteIconClick(it))
                            },
                            onPalettePicked = { onEvent(WhiteboardEvent.OnPalettePicked(it)) },
                            isPaletteEditMode = state.isPaletteEditMode,
                            changeEditMode = { onEvent(WhiteboardEvent.OnPaletteEditMode) },
                            onPaletteAdded = { onEvent(WhiteboardEvent.OnPaletteAdded(it)) },
                            onPaletteRemoved = { onEvent(WhiteboardEvent.ShowRemovePaletteDialog(it)) },
                            onCloseIconClick = {
                                scope.launch { drawerState.close() }
                                focusManager.clearFocus()
                            },
                            colorDeletionMode = state.canvasColorDeletionMode,
                            onSetColorDeletionMode = { onEvent(WhiteboardEvent.SetColorDeletionMode(it, ColorPaletteType.CANVAS)) },
                            onColorDeleted = { color: Color, palette: ColorPaletteType ->
                                onEvent(WhiteboardEvent.OnColorDeleted(color, palette))
                            }
                        )
                    },
                ) {
                    DrawingCanvas(
                        modifier = Modifier.fillMaxSize()
                            .onSizeChanged { size -> onEvent(WhiteboardEvent.CanvasSizeChanged(size)) }
                            .pointerInput(Unit) {
                                awaitPointerEventScope {
                                    while (true) {
                                        val down = awaitFirstDown(requireUnconsumed = false)
                                        onEvent(WhiteboardEvent.OnCardClose)
                                        onEvent(WhiteboardEvent.OnStrokeWidthSliderClose)
                                        onEvent(WhiteboardEvent.OnDrawingToolDialogClose)
                                        onEvent(WhiteboardEvent.ZoomSliderVisibilityChange(false))
                                        focusManager.clearFocus()
                                        down.consume()
                                    }
                                }},
                        state = state,
                        onEvent = onEvent
                    )

                    CommandBarHorizontal(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(10.dp),
                        backgroundColor = state.canvasColor,
                        canvasScale = state.canvasScale,
                        selectedDrawingTool = state.selectedDrawingTool,
                        isZoomSliderOpen = state.isZoomSliderOpen,
                        isUndoEnabled = state.updatePointer != null,
                        isRedoEnabled = state.undoArray.isNotEmpty() && (state.updatePointer ?: -1) < state.updates.size,
                        onHomeIconClick = {
                            onEvent(WhiteboardEvent.SaveMiniature(scope))
                            onHomeIconClick.invoke()
                        },
                        onMenuIconClick = { scope.launch { drawerState.open() } },
                        onSaveIconClick = {
                            if (!state.grantedExternalStoragePermission)
                                requestPicturePermission(
                                    true,
                                    {
                                        onEvent(WhiteboardEvent.OnAskedForPermissions)
                                        onEvent(WhiteboardEvent.OnPicturePermissionChanged(true))
                                        onEvent(WhiteboardEvent.SavePicture(scope))
                                        scope.launch { snackbarHostState.showSnackbar(message = "Picture saved!") }
                                    },
                                    {
                                        onEvent(WhiteboardEvent.OnAskedForPermissions)
                                        onEvent(WhiteboardEvent.OnPicturePermissionChanged(false))
                                        scope.launch { snackbarHostState.showSnackbar(message = "Permission is required to save pictures") }
                                    }
                                )
                            else {
                                onEvent(WhiteboardEvent.SavePicture(scope))
                                scope.launch { snackbarHostState.showSnackbar(message = "Picture saved!") }
                            }
                        },
                        onUndoIconClick = { onEvent(WhiteboardEvent.Undo) },
                        onRedoIconClick = { onEvent(WhiteboardEvent.Redo) },
                        onZoomButtonClick = { onEvent(WhiteboardEvent.ZoomSliderVisibilityChange()) },
                        onZoomButtonDoubleClick = {
                            onEvent(WhiteboardEvent.CanvasZoomChange(1.0f))
                            onEvent(WhiteboardEvent.ZoomSliderVisibilityChange(visible = false))
                        }
                    )
                    Row(
                        modifier = Modifier.align(Alignment.BottomStart)
                    ) {
                        MarkerColorBar(
                            modifier = Modifier.padding(horizontal = 10.dp)
                                .align(Alignment.Bottom),
                            penWidth = 24.dp,
                            penHeight = 48.dp,
                            padding = 6.dp,
                            markerColors = state.markerColors,
                            selectedMarker = state.selectedMarker,
                            selectedDrawingTool = state.selectedDrawingTool,
                            drawingToolVisibility = state.drawingToolVisibility,
                            onClick = { newColor: Color ->
                                onEvent(WhiteboardEvent.StrokeColorChange(newColor, false))
                                onEvent(WhiteboardEvent.OnStrokeWidthSliderClose)
                                onEvent(WhiteboardEvent.OnCommandPaletteClose)
                                onEvent(WhiteboardEvent.OnDrawingToolDialogClose)
                                onEvent(WhiteboardEvent.ZoomSliderVisibilityChange(false))
                            },
                            onEraserClick = {
                                eraserType: DrawingTool -> onEvent(WhiteboardEvent.OnDrawingToolSelected(eraserType))
                                onEvent(WhiteboardEvent.OnCardClose)
                                onEvent(WhiteboardEvent.OnStrokeWidthSliderClose)
                                onEvent(WhiteboardEvent.OnCommandPaletteClose)
                                onEvent(WhiteboardEvent.OnDrawingToolDialogClose)
                                onEvent(WhiteboardEvent.ZoomSliderVisibilityChange(false))
                            }
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.Bottom)
                        ) {
                            StrokeWidthBar(
                                modifier = Modifier.height(42.dp),
                                minButtonSize = 12.dp,
                                maxButtonSize = 30.dp,
                                strokeWidthList = state.strokeWidthList,
                                activeButton = state.activeStrokeWidthButton,
                                canvasColor = state.canvasColor,
                                onClick = { strokeNum: Int ->
                                    onEvent(WhiteboardEvent.StrokeWidthButtonClicked(strokeNum))
                                    onEvent(WhiteboardEvent.OnCardClose)
                                    onEvent(WhiteboardEvent.OnCommandPaletteClose)
                                    onEvent(WhiteboardEvent.OnDrawingToolDialogClose)
                                    onEvent(WhiteboardEvent.ZoomSliderVisibilityChange(false))
                                }
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(end = 10.dp, bottom = 6.dp),
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            DrawingToolDialog(
                                modifier = Modifier.padding(bottom = 24.dp),
                                isDrawingToolDialogVisible = state.isDrawingToolDialogOpen,
                                backgroundColor = state.canvasColor,
                                drawingToolVisibility = state.drawingToolVisibility,
                                currentDrawingTool = if (!state.selectedDrawingTool.isErasing()) state.selectedDrawingTool else state.previousDrawingTool,
                                onDrawingToolClick = { drawingTool: DrawingTool ->
                                    onEvent(WhiteboardEvent.OnDrawingToolSelected(drawingTool))
                                    onEvent(WhiteboardEvent.OnStrokeWidthSliderClose)
                                    onEvent(WhiteboardEvent.OnCardClose)
                                    onEvent(WhiteboardEvent.OnCommandPaletteClose)
                                    onEvent(WhiteboardEvent.OnDrawingToolDialogClose)
                                    onEvent(WhiteboardEvent.ZoomSliderVisibilityChange(false))
                                }
                            )
                            ElevatedIconButton(
                                backgroundColor = state.canvasColor,
                                size = 46.dp,
                                isSelected = true,
                                onClick = {
                                    onEvent(WhiteboardEvent.OnDrawingToolButtonClick)
                                    onEvent(WhiteboardEvent.OnStrokeWidthSliderClose)
                                    onEvent(WhiteboardEvent.OnCardClose)
                                    onEvent(WhiteboardEvent.OnCommandPaletteClose)
                                    onEvent(WhiteboardEvent.ZoomSliderVisibilityChange(false))
                                }) {
                                Icon(
                                    painter = painterResource(
                                        if (!state.selectedDrawingTool.isErasing()) state.selectedDrawingTool.res
                                        else state.previousDrawingTool.res
                                    ),
                                    contentDescription = state.selectedDrawingTool.name.lowercase().replaceFirstChar { it.uppercaseChar() },
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }
                    }
                    ColorPickerCard(
                        modifier = Modifier
                            .padding(start = 16.dp * state.selectedMarker, bottom = 30.dp)
                            .align(Alignment.BottomStart),
                        isVisible = state.isColorPickerOpen,
                        selectedDrawingTool = state.selectedDrawingTool,
                        strokeColors = state.preferredStrokeColors,
                        selectedStrokeColor = state.strokeColor,
                        onStrokeColorChange = { newColor: Color ->
                            onEvent(WhiteboardEvent.StrokeColorChange(newColor, true))
                        },
                        fillColors = state.preferredFillColors,
                        selectedFillColor = state.fillColor,
                        onFillColorChange =  { newColor: Color ->
                            onEvent(WhiteboardEvent.FillColorChange(newColor))
                        },
                        colorDeletionMode = state.markerColorDeletionMode,
                        onSetColorDeletionMode = { mode: Boolean -> onEvent(WhiteboardEvent.SetColorDeletionMode(mode, ColorPaletteType.MARKER))},
                        onColorDeleted = { color: Color, palette: ColorPaletteType ->
                            onEvent(WhiteboardEvent.OnColorDeleted(color, palette)) },
                        onColorPaletteIconClick = { colorPaletteType: ColorPaletteType ->
                            onEvent(WhiteboardEvent.OnColorPaletteIconClick(colorPaletteType))
                        },
                        onCloseIconClick = { onEvent(WhiteboardEvent.OnCardClose) }
                    )
                    StrokeWidthSliderCard(
                        modifier = Modifier
                            .padding(bottom = 30.dp)
                            .align(Alignment.BottomEnd),
                        width = 218.dp,
                        isVisible = state.isStrokeWidthSliderOpen,
                        showOpacity = state.showOpacitySlider,
                        strokeWidthSliderValue = state.strokeWidthList[state.activeStrokeWidthButton],
                        onStrokeWidthSliderValueChange = { strokeWidth: Float -> onEvent(WhiteboardEvent.StrokeSliderValueChange(strokeWidth)) },
                        opacitySliderValue = state.opacity,
                        onOpacitySliderValueChange = { opacity: Float -> onEvent(WhiteboardEvent.OpacitySliderValueChange(opacity)) },
                        onCloseIconClick = { onEvent(WhiteboardEvent.OnStrokeWidthSliderClose) }
                    )
                    ZoomSliderCard(
                        modifier = Modifier
                            .padding(top = 30.dp, start = 6.dp)
                            .align(Alignment.TopStart),
                        width = 256.dp,
                        isVisible = state.isZoomSliderOpen,
                        zoomSliderValue = state.canvasScale,
                        onZoomSliderValueChange = { zoom -> onEvent(WhiteboardEvent.CanvasZoomChange(zoom)) },
                        onCloseIconClick = { onEvent(WhiteboardEvent.ZoomSliderVisibilityChange(false)) }
                    )
                }
                Box(modifier = Modifier.fillMaxSize()) {
                    SnackbarHost(
                        hostState = snackbarHostState,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }

            UiType.MEDIUM -> {
                DrawingCanvas(
                    modifier = Modifier.fillMaxSize()
                        .onSizeChanged { size -> onEvent(WhiteboardEvent.CanvasSizeChanged(size)) }
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    val down = awaitFirstDown(requireUnconsumed = false)
                                    onEvent(WhiteboardEvent.OnCardClose)
                                    onEvent(WhiteboardEvent.OnStrokeWidthSliderClose)
                                    onEvent(WhiteboardEvent.OnCommandPaletteClose)
                                    onEvent(WhiteboardEvent.ZoomSliderVisibilityChange(false))
                                    focusManager.clearFocus()
                                    down.consume()
                                }
                            }},
                    state = state,
                    onEvent = onEvent
                )
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                ) {
                    CommandBarVertical(
                        onHomeIconClick = {
                            onEvent(WhiteboardEvent.SaveMiniature(scope))
                            onHomeIconClick.invoke()
                        },
                        backgroundColor = state.canvasColor,
                        canvasScale = state.canvasScale,
                        selectedDrawingTool = state.selectedDrawingTool,
                        isZoomSliderOpen = state.isZoomSliderOpen,
                        isUndoEnabled = state.updatePointer != null,
                        isRedoEnabled = state.undoArray.isNotEmpty() && (state.updatePointer ?: -1) < state.updates.size,
                        onMenuIconClick = { onEvent(WhiteboardEvent.OnCommandPaletteIconClick) },
                        onSaveIconClick = {
                            if (!state.grantedExternalStoragePermission)
                                requestPicturePermission(
                                    true,
                                    {
                                        onEvent(WhiteboardEvent.OnAskedForPermissions)
                                        onEvent(WhiteboardEvent.OnPicturePermissionChanged(true))
                                        onEvent(WhiteboardEvent.SavePicture(scope))
                                        scope.launch { snackbarHostState.showSnackbar(message = "Picture saved!") }
                                    },
                                    {
                                        onEvent(WhiteboardEvent.OnAskedForPermissions)
                                        onEvent(WhiteboardEvent.OnPicturePermissionChanged(false))
                                        scope.launch { snackbarHostState.showSnackbar(message = "Permission is required to save pictures") }
                                    }
                                )
                            else {
                                onEvent(WhiteboardEvent.SavePicture(scope))
                                scope.launch { snackbarHostState.showSnackbar(message = "Picture saved!") }
                            }
                        },
                        onUndoIconClick = { onEvent(WhiteboardEvent.Undo) },
                        onRedoIconClick = { onEvent(WhiteboardEvent.Redo) },
                        onZoomButtonClick = { onEvent(WhiteboardEvent.ZoomSliderVisibilityChange()) },
                        onZoomButtonDoubleClick = {
                            onEvent(WhiteboardEvent.CanvasZoomChange(1.0f))
                            onEvent(WhiteboardEvent.ZoomSliderVisibilityChange(visible = false))
                        }
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    CommandPaletteCard(
                        isVisible = state.isCommandPaletteOpen,
                        title = state.whiteboardName,
                        canvasColors = state.preferredCanvasColors,
                        selectedCanvasColor = state.canvasColor,
                        palettes = state.paletteList,
                        currentPalette = state.palette,
                        onTitleChange = { onEvent(WhiteboardEvent.OnTitleChange(it)) },
                        onCanvasColorChange = { onEvent(WhiteboardEvent.CanvasColorChange(it)) },
                        onColorPaletteIconClick = {
                            onEvent(WhiteboardEvent.OnColorPaletteIconClick(it))
                        },
                        onPalettePicked = { onEvent(WhiteboardEvent.OnPalettePicked(it)) },
                        isPaletteEditMode = state.isPaletteEditMode,
                        changeEditMode = { onEvent(WhiteboardEvent.OnPaletteEditMode) },
                        onPaletteAdded = { onEvent(WhiteboardEvent.OnPaletteAdded(it)) },
                        onPaletteRemoved = { onEvent(WhiteboardEvent.ShowRemovePaletteDialog(it)) },
                        onCloseIconClick = { onEvent(WhiteboardEvent.OnCommandPaletteClose) },
                        colorDeletionMode = state.canvasColorDeletionMode,
                        onSetColorDeletionMode = { onEvent(WhiteboardEvent.SetColorDeletionMode(it, ColorPaletteType.CANVAS)) },
                        onColorDeleted = { color: Color, palette: ColorPaletteType ->
                            onEvent(WhiteboardEvent.OnColorDeleted(color, palette))
                        }
                    )
                }
                ZoomSliderCard(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 32.dp, top = 102.dp),
                    width = 288.dp,
                    isVisible = state.isZoomSliderOpen,
                    zoomSliderValue = state.canvasScale,
                    onZoomSliderValueChange = { zoom -> onEvent(WhiteboardEvent.CanvasZoomChange(zoom)) },
                    onCloseIconClick = { onEvent(WhiteboardEvent.ZoomSliderVisibilityChange(false)) }
                )
                ColorPickerCard(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 16.dp * state.selectedMarker, bottom = 30.dp),
                    isVisible = state.isColorPickerOpen,
                    selectedDrawingTool = state.selectedDrawingTool,
                    strokeColors = state.preferredStrokeColors,
                    selectedStrokeColor = state.strokeColor,
                    onStrokeColorChange = { newColor: Color ->
                        onEvent(WhiteboardEvent.StrokeColorChange(newColor, true))
                    },
                    fillColors = state.preferredFillColors,
                    selectedFillColor = state.fillColor,
                    onFillColorChange =  { newColor: Color ->
                        onEvent(WhiteboardEvent.FillColorChange(newColor))
                    },
                    colorDeletionMode = state.markerColorDeletionMode,
                    onSetColorDeletionMode = { mode: Boolean -> onEvent(WhiteboardEvent.SetColorDeletionMode(mode, ColorPaletteType.MARKER))},
                    onColorDeleted = { color: Color, palette: ColorPaletteType ->
                        onEvent(WhiteboardEvent.OnColorDeleted(color, palette)) },
                    onColorPaletteIconClick = { colorPaletteType: ColorPaletteType ->
                        onEvent(WhiteboardEvent.OnColorPaletteIconClick(colorPaletteType))
                    },
                    onCloseIconClick = { onEvent(WhiteboardEvent.OnCardClose) }
                )
                Row(
                    modifier = Modifier.align(Alignment.BottomStart)
                ) {
                    Column(
                        modifier = Modifier.align(Alignment.Bottom)
                    ) {
                        MarkerColorBar(
                            penWidth = 24.dp,
                            penHeight = 48.dp,
                            padding = 6.dp,
                            markerColors = state.markerColors,
                            selectedMarker = state.selectedMarker,
                            selectedDrawingTool = state.selectedDrawingTool,
                            drawingToolVisibility = state.drawingToolVisibility,
                            onClick = { newColor: Color ->
                                onEvent(WhiteboardEvent.StrokeColorChange(newColor, false))
                                onEvent(WhiteboardEvent.OnStrokeWidthSliderClose)
                                onEvent(WhiteboardEvent.OnCommandPaletteClose)
                                onEvent(WhiteboardEvent.ZoomSliderVisibilityChange(false))
                            },
                            onEraserClick = {
                                eraserType: DrawingTool -> onEvent(WhiteboardEvent.OnDrawingToolSelected(eraserType))
                                onEvent(WhiteboardEvent.OnCardClose)
                                onEvent(WhiteboardEvent.OnStrokeWidthSliderClose)
                                onEvent(WhiteboardEvent.OnCommandPaletteClose)
                                onEvent(WhiteboardEvent.OnDrawingToolDialogClose)
                                onEvent(WhiteboardEvent.ZoomSliderVisibilityChange(false))
                            }
                        )
                    }
                    Column(
                        modifier = Modifier
                            .align(Alignment.Bottom)
                            .padding(start = 12.dp)
                    ) {
                        StrokeWidthSliderCard(
                            modifier = Modifier.padding(start = 16.dp * state.activeStrokeWidthButton, bottom = 6.dp),
                            isVisible = state.isStrokeWidthSliderOpen,
                            showOpacity = state.showOpacitySlider,
                            strokeWidthSliderValue = state.strokeWidthList[state.activeStrokeWidthButton],
                            onStrokeWidthSliderValueChange = { strokeWidth: Float -> onEvent(WhiteboardEvent.StrokeSliderValueChange(strokeWidth)) },
                            opacitySliderValue = state.opacity,
                            onOpacitySliderValueChange = { opacity: Float -> onEvent(WhiteboardEvent.OpacitySliderValueChange(opacity)) },
                            onCloseIconClick = { onEvent(WhiteboardEvent.OnStrokeWidthSliderClose) }
                        )
                        StrokeWidthBar(
                            modifier = Modifier.height(42.dp),
                            minButtonSize = 12.dp,
                            maxButtonSize = 30.dp,
                            strokeWidthList = state.strokeWidthList,
                            activeButton = state.activeStrokeWidthButton,
                            canvasColor = state.canvasColor,
                            onClick = { strokeNum: Int ->
                                onEvent(WhiteboardEvent.StrokeWidthButtonClicked(strokeNum))
                                onEvent(WhiteboardEvent.OnCardClose)
                                onEvent(WhiteboardEvent.OnCommandPaletteClose)
                                onEvent(WhiteboardEvent.ZoomSliderVisibilityChange(false))
                            }
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(end = 10.dp, bottom = 6.dp, top = 10.dp),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        DrawingToolDialog(
                            modifier = Modifier.padding(bottom = 10.dp),
                            isDrawingToolDialogVisible = state.isDrawingToolDialogOpen,
                            backgroundColor = state.canvasColor,
                            drawingToolVisibility = state.drawingToolVisibility,
                            currentDrawingTool = if (!state.selectedDrawingTool.isErasing()) state.selectedDrawingTool else state.previousDrawingTool,
                            onDrawingToolClick = { drawingTool: DrawingTool ->
                                onEvent(WhiteboardEvent.OnDrawingToolSelected(drawingTool))
                                onEvent(WhiteboardEvent.OnStrokeWidthSliderClose)
                                onEvent(WhiteboardEvent.OnCardClose)
                                onEvent(WhiteboardEvent.OnCommandPaletteClose)
                                onEvent(WhiteboardEvent.OnDrawingToolDialogClose)
                                onEvent(WhiteboardEvent.ZoomSliderVisibilityChange(false))
                            }
                        )
                        ElevatedIconButton(
                            backgroundColor = state.canvasColor,
                            size = 46.dp,
                            isSelected = true,
                            onClick = {
                                onEvent(WhiteboardEvent.OnDrawingToolButtonClick)
                                onEvent(WhiteboardEvent.OnStrokeWidthSliderClose)
                                onEvent(WhiteboardEvent.OnCardClose)
                                onEvent(WhiteboardEvent.OnCommandPaletteClose)
                                onEvent(WhiteboardEvent.ZoomSliderVisibilityChange(false))
                            }) {
                            Icon(
                                painter = painterResource(
                                    if (!state.selectedDrawingTool.isErasing()) state.selectedDrawingTool.res
                                    else state.previousDrawingTool.res
                                ),
                                contentDescription = state.selectedDrawingTool.name.lowercase().replaceFirstChar { it.uppercaseChar() },
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                }
                Box(modifier = Modifier.fillMaxSize()) {
                    SnackbarHost(
                        hostState = snackbarHostState,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }

            UiType.EXPANDED -> {
                DrawingCanvas(
                    modifier = Modifier.fillMaxSize()
                        .onSizeChanged { size -> onEvent(WhiteboardEvent.CanvasSizeChanged(size)) }
                        .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                onEvent(WhiteboardEvent.OnCardClose)
                                onEvent(WhiteboardEvent.OnStrokeWidthSliderClose)
                                onEvent(WhiteboardEvent.OnCommandPaletteClose)
                                onEvent(WhiteboardEvent.ZoomSliderVisibilityChange(false))
                                focusManager.clearFocus()
                                down.consume()
                            }
                        }},
                    state = state,
                    onEvent = onEvent
                )
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                ) {
                    CommandBarVertical(
                        onHomeIconClick = {
                            onEvent(WhiteboardEvent.SaveMiniature(scope))
                            onHomeIconClick.invoke()
                        },
                        backgroundColor = state.canvasColor,
                        canvasScale = state.canvasScale,
                        selectedDrawingTool = state.selectedDrawingTool,
                        isZoomSliderOpen = state.isZoomSliderOpen,
                        isUndoEnabled = state.updatePointer != null,
                        isRedoEnabled = state.undoArray.isNotEmpty() && (state.updatePointer ?: -1) < state.updates.size,
                        onMenuIconClick = { onEvent(WhiteboardEvent.OnCommandPaletteIconClick) },
                        onSaveIconClick = {
                            if (!state.grantedExternalStoragePermission)
                                requestPicturePermission(
                                    true,
                                    {
                                        onEvent(WhiteboardEvent.OnAskedForPermissions)
                                        onEvent(WhiteboardEvent.OnPicturePermissionChanged(true))
                                        onEvent(WhiteboardEvent.SavePicture(scope))
                                        scope.launch { snackbarHostState.showSnackbar(message = "Picture saved!") }
                                    },
                                    {
                                        onEvent(WhiteboardEvent.OnAskedForPermissions)
                                        onEvent(WhiteboardEvent.OnPicturePermissionChanged(false))
                                        scope.launch { snackbarHostState.showSnackbar(message = "Permission is required to save pictures") }
                                    }
                                )
                            else {
                                onEvent(WhiteboardEvent.SavePicture(scope))
                                scope.launch { snackbarHostState.showSnackbar(message = "Picture saved!") }
                            }
                        },
                        onUndoIconClick = { onEvent(WhiteboardEvent.Undo) },
                        onRedoIconClick = { onEvent(WhiteboardEvent.Redo) },
                        onZoomButtonClick = { onEvent(WhiteboardEvent.ZoomSliderVisibilityChange()) },
                        onZoomButtonDoubleClick = {
                            onEvent(WhiteboardEvent.CanvasZoomChange(1.0f))
                            onEvent(WhiteboardEvent.ZoomSliderVisibilityChange(visible = false))
                        }
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    CommandPaletteCard(
                        isVisible = state.isCommandPaletteOpen,
                        title = state.whiteboardName,
                        canvasColors = state.preferredCanvasColors,
                        selectedCanvasColor = state.canvasColor,
                        palettes = state.paletteList,
                        currentPalette = state.palette,
                        onTitleChange = { onEvent(WhiteboardEvent.OnTitleChange(it)) },
                        onCanvasColorChange = { onEvent(WhiteboardEvent.CanvasColorChange(it)) },
                        onColorPaletteIconClick = {
                            onEvent(WhiteboardEvent.OnColorPaletteIconClick(it))
                        },
                        onPalettePicked = { onEvent(WhiteboardEvent.OnPalettePicked(it)) },
                        isPaletteEditMode = state.isPaletteEditMode,
                        changeEditMode = { onEvent(WhiteboardEvent.OnPaletteEditMode) },
                        onPaletteAdded = { onEvent(WhiteboardEvent.OnPaletteAdded(it)) },
                        onPaletteRemoved = { onEvent(WhiteboardEvent.ShowRemovePaletteDialog(it)) },
                        onCloseIconClick = { onEvent(WhiteboardEvent.OnCommandPaletteClose) },
                        colorDeletionMode = state.canvasColorDeletionMode,
                        onSetColorDeletionMode = { onEvent(WhiteboardEvent.SetColorDeletionMode(it, ColorPaletteType.CANVAS)) },
                        onColorDeleted = { color: Color, palette: ColorPaletteType ->
                            onEvent(WhiteboardEvent.OnColorDeleted(color, palette))
                        }
                    )
                }
                ZoomSliderCard(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 32.dp, top = 102.dp),
                    width = 304.dp,
                    isVisible = state.isZoomSliderOpen,
                    zoomSliderValue = state.canvasScale,
                    onZoomSliderValueChange = { zoom -> onEvent(WhiteboardEvent.CanvasZoomChange(zoom)) },
                    onCloseIconClick = { onEvent(WhiteboardEvent.ZoomSliderVisibilityChange(false)) }
                )
                DrawingToolBar(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp),
                    backgroundColor = state.canvasColor,
                    drawingToolVisibility = state.drawingToolVisibility,
                    currentDrawingTool = if (!state.selectedDrawingTool.isErasing()) state.selectedDrawingTool else state.previousDrawingTool,
                    onDrawingToolClick = { drawingTool: DrawingTool ->
                        onEvent(WhiteboardEvent.OnDrawingToolSelected(drawingTool))
                        onEvent(WhiteboardEvent.OnStrokeWidthSliderClose)
                        onEvent(WhiteboardEvent.OnCardClose)
                        onEvent(WhiteboardEvent.OnCommandPaletteClose)
                        onEvent(WhiteboardEvent.ZoomSliderVisibilityChange(false))
                    }
                )
                ColorPickerCard(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 20.dp * state.selectedMarker, bottom = 36.dp),
                    isVisible = state.isColorPickerOpen,
                    selectedDrawingTool = state.selectedDrawingTool,
                    strokeColors = state.preferredStrokeColors,
                    selectedStrokeColor = state.strokeColor,
                    onStrokeColorChange = { newColor: Color ->
                        onEvent(WhiteboardEvent.StrokeColorChange(newColor, true))
                    },
                    fillColors = state.preferredFillColors,
                    selectedFillColor = state.fillColor,
                    onFillColorChange =  { newColor: Color ->
                        onEvent(WhiteboardEvent.FillColorChange(newColor))
                    },
                    colorDeletionMode = state.markerColorDeletionMode,
                    onSetColorDeletionMode = { mode: Boolean -> onEvent(WhiteboardEvent.SetColorDeletionMode(mode, ColorPaletteType.MARKER))},
                    onColorDeleted = { color: Color, palette: ColorPaletteType ->
                        onEvent(WhiteboardEvent.OnColorDeleted(color, palette)) },
                    onColorPaletteIconClick = { colorPaletteType: ColorPaletteType ->
                        onEvent(WhiteboardEvent.OnColorPaletteIconClick(colorPaletteType))
                    },
                    onCloseIconClick = { onEvent(WhiteboardEvent.OnCardClose) }
                )
                Row(
                    modifier = Modifier.align(Alignment.BottomStart)
                ) {
                    Column(
                        modifier = Modifier.align(Alignment.Bottom)
                    ) {
                        MarkerColorBar(
                            penWidth = 30.dp,
                            penHeight = 60.dp,
                            padding = 10.dp,
                            markerColors = state.markerColors,
                            selectedMarker = state.selectedMarker,
                            selectedDrawingTool = state.selectedDrawingTool,
                            drawingToolVisibility = state.drawingToolVisibility,
                            onClick = { newColor: Color ->
                                onEvent(WhiteboardEvent.StrokeColorChange(newColor, false))
                                onEvent(WhiteboardEvent.OnStrokeWidthSliderClose)
                                onEvent(WhiteboardEvent.OnCommandPaletteClose)
                                onEvent(WhiteboardEvent.ZoomSliderVisibilityChange(false))
                            },
                            onEraserClick = {
                                eraserType: DrawingTool -> onEvent(WhiteboardEvent.OnDrawingToolSelected(eraserType))
                                onEvent(WhiteboardEvent.OnCardClose)
                                onEvent(WhiteboardEvent.OnStrokeWidthSliderClose)
                                onEvent(WhiteboardEvent.OnCommandPaletteClose)
                                onEvent(WhiteboardEvent.OnDrawingToolDialogClose)
                                onEvent(WhiteboardEvent.ZoomSliderVisibilityChange(false))
                            }
                        )
                    }
                    Column(
                        modifier = Modifier
                            .align(Alignment.Bottom)
                            .padding(start = 5.dp)
                    ) {
                        StrokeWidthSliderCard(
                            modifier = Modifier.padding(start = 20.dp * state.activeStrokeWidthButton, bottom = 6.dp),
                            isVisible = state.isStrokeWidthSliderOpen,
                            showOpacity = state.showOpacitySlider,
                            strokeWidthSliderValue = state.strokeWidthList[state.activeStrokeWidthButton],
                            onStrokeWidthSliderValueChange = { strokeWidth: Float -> onEvent(WhiteboardEvent.StrokeSliderValueChange(strokeWidth)) },
                            opacitySliderValue = state.opacity,
                            onOpacitySliderValueChange = { opacity: Float -> onEvent(WhiteboardEvent.OpacitySliderValueChange(opacity)) },
                            onCloseIconClick = { onEvent(WhiteboardEvent.OnStrokeWidthSliderClose) }
                        )
                        StrokeWidthBar(
                            modifier = Modifier.height(56.dp),
                            minButtonSize = 12.dp,
                            maxButtonSize = 40.dp,
                            strokeWidthList = state.strokeWidthList,
                            activeButton = state.activeStrokeWidthButton,
                            canvasColor = state.canvasColor,
                            onClick = { strokeNum: Int ->
                                onEvent(WhiteboardEvent.StrokeWidthButtonClicked(strokeNum))
                                onEvent(WhiteboardEvent.OnCardClose)
                                onEvent(WhiteboardEvent.OnCommandPaletteClose)
                                onEvent(WhiteboardEvent.ZoomSliderVisibilityChange(false))
                            }
                        )
                    }
                }
                Image(
                    painter = painterResource(Res.drawable.logoWithName),
                    contentDescription = "Best church is the church as a priority",
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .height(144.dp)
                        .absoluteOffset(16.dp),
                    colorFilter = ColorFilter.tint(
                        if (state.canvasColor.luminance() > 0.5) Color.Black else Color.White
                    )
                )
                Box(modifier = Modifier.fillMaxSize()) {
                    SnackbarHost(
                        hostState = snackbarHostState,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }

    }
}

@Composable
private fun DrawingCanvas(
    modifier: Modifier = Modifier,
    state: WhiteboardState,
    onEvent: (WhiteboardEvent) -> Unit
) {

    Canvas(
        modifier = modifier
            .capturable(state.captureController)
            .background(state.canvasColor)
            .pointerInput(Unit) {
                detectStylusDragGestures(
                    stylusInput = state.stylusInput,
                    onDragStart = { offset ->
                        onEvent(WhiteboardEvent.StartDrawing(offset))
                    },
                    onDrag = { change, _ ->
                        val offset = Offset(x = change.position.x, y = change.position.y)
                        onEvent(WhiteboardEvent.ContinueDrawing(offset))
                    },
                    onDragEnd = {
                        onEvent(WhiteboardEvent.FinishDrawing)
                    }
                )
            }
            .pointerInput(state.selectedDrawingTool) {
                if (state.selectedDrawingTool != DrawingTool.CANVAS_PANNER) return@pointerInput

                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    var previousPosition = down.position

                    do {
                        val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                        val pressed = event.changes.filter { it.pressed }

                        if (pressed.isEmpty()) break
                        if (pressed.size == 2) break

                        if (pressed.size == 1) {
                            val current = pressed.first().position
                            val delta = current - previousPosition

                            if (delta != Offset.Zero) {
                                onEvent(WhiteboardEvent.CanvasTransformed(
                                    center = current,
                                    offset = delta,
                                    zoomChange = 1f
                                ))
                            }

                            previousPosition = current
                            event.changes.forEach { it.consume() }
                        }
                    } while (event.changes.any { it.pressed })
                }
            }
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)

                    var previousCenter = Offset.Zero
                    var previousSpan = 0f

                    do {
                        val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                        val pressed = event.changes.filter { it.pressed }

                        if (pressed.size >= 2) {
                            val center = pressed
                                .map { it.position }
                                .fold(Offset.Zero) { acc, pos -> acc + pos } / pressed.size.toFloat()

                            val span = pressed
                                .map { (it.position - center).getDistance() }
                                .average()
                                .toFloat()
                                .coerceAtLeast(0.01f)

                            if (previousSpan == 0f) {
                                previousCenter = center
                                previousSpan = span
                            } else {
                                val zoomChange = span / previousSpan
                                val offset = center - previousCenter

                                onEvent(WhiteboardEvent.CanvasTransformed(center, offset, zoomChange))

                                previousCenter = center
                                previousSpan = span
                            }

                            event.changes.forEach { it.consume() }
                        }
                    } while (event.changes.any { it.pressed })
                }
            }
    ) {
        translate(top = state.canvasOffset.y, left = state.canvasOffset.x) {
            scale(scale = state.canvasScale, pivot = Offset.Zero) {
                state.paths.forEach { path ->
                    drawCustomPath(path)
                }

                state.currentPath?.let { path ->
                    drawCustomPath(path)
                }
            }
        }
    }
    AnimateLaserPath(
        laserPenPath = state.laserPenPath,
        state,
        onPathAnimationComplete = { onEvent(WhiteboardEvent.OnLaserPathAnimationComplete) }
    )
}

private fun DrawScope.drawCustomPath(path: DrawnPath) {
    val pathOpacity = path.opacity / 100

    when (path.fillColor) {
        Color.Transparent -> {
            drawPath(
                path = path.path,
                color = path.strokeColor.copy(alpha = pathOpacity),
                style = Stroke(
                    width = path.strokeWidth.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }

        else -> {
            drawPath(
                path = path.path,
                color = path.fillColor.copy(alpha = pathOpacity),
                style = Fill
            )
            drawPath(
                path = path.path,
                color = path.strokeColor.copy(alpha = pathOpacity),
                style = Stroke(
                    width = path.strokeWidth.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
    }
}

@Composable
fun AnimateLaserPath(
    laserPenPath: DrawnPath?,
    state: WhiteboardState,
    onPathAnimationComplete: () -> Unit
) {
    val animationProgress = remember { Animatable(initialValue = 1f) }
    val pathMeasure = remember { PathMeasure() }

    LaunchedEffect(laserPenPath) {
        animationProgress.snapTo(1f)
        laserPenPath?.let {
            pathMeasure.setPath(it.path, false)
            animationProgress.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 1000)
            )
            onPathAnimationComplete()
        }
    }

    val trimmedPath = Path()
    if (laserPenPath != null) {
        pathMeasure.apply {
            setPath(path = laserPenPath.path, forceClosed = false)
            getSegment(
                startDistance = length * (1 - animationProgress.value),
                stopDistance = length,
                destination = trimmedPath
            )
        }
    }

    Canvas(modifier = Modifier) {
        translate(top = state.canvasOffset.y, left = state.canvasOffset.x) {
            scale(scale = state.canvasScale, pivot = Offset.Zero) {
                laserPenPath?.let {
                    drawPath(
                        path = trimmedPath,
                        color = laserPenPath.strokeColor,
                        style = Stroke(width = laserPenPath.strokeWidth.dp.toPx())
                    )
                }
            }
        }
    }
}

@Composable
private fun miniatureSaveHandle(scope: CoroutineScope, onEvent: (WhiteboardEvent) -> Unit, navController: NavController) {
    registerBackHandler {
        onEvent(WhiteboardEvent.SaveMiniature(scope))
        navController.navigateUp()
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(Unit) {
        val observer = object : DefaultLifecycleObserver {
            override fun onPause(owner: LifecycleOwner) {
                onEvent(WhiteboardEvent.SaveMiniature(scope))
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}

@Composable
expect fun HideSystemBars()


