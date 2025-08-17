package org.samis.whiteboard.presentation.whiteboard

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
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
import org.samis.whiteboard.presentation.theme.Palettes
import org.samis.whiteboard.presentation.util.UiType
import org.samis.whiteboard.presentation.util.capturable
import org.samis.whiteboard.presentation.util.getUiType
import org.samis.whiteboard.presentation.util.registerBackHandler
import org.samis.whiteboard.presentation.util.rememberCaptureController
import org.samis.whiteboard.presentation.util.rememberScreenSizeSize
import org.samis.whiteboard.presentation.whiteboard.component.ColorPickerCard
import org.samis.whiteboard.presentation.whiteboard.component.ColorSelectionDialog
import org.samis.whiteboard.presentation.whiteboard.component.CommandBarHorizontal
import org.samis.whiteboard.presentation.whiteboard.component.CommandBarVertical
import org.samis.whiteboard.presentation.whiteboard.component.CommandPaletteCard
import org.samis.whiteboard.presentation.whiteboard.component.CommandPaletteDrawerContent
import org.samis.whiteboard.presentation.whiteboard.component.DrawingToolBar
import org.samis.whiteboard.presentation.whiteboard.component.MarkerColorBar
import org.samis.whiteboard.presentation.whiteboard.component.StrokeWidthBar
import org.samis.whiteboard.presentation.whiteboard.component.StrokeWidthSliderCard
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

    var isCommandPaletteOpen by rememberSaveable { mutableStateOf(false) }

    miniatureSaveHandle(scope, onEvent, navController)

    ColorSelectionDialog(
        isOpen = state.isColorSelectionDialogOpen,
        onColorSelected = { onEvent(WhiteboardEvent.OnColorSelected(it)) },
        onDismiss = { onEvent(WhiteboardEvent.ColorSelectionDialogDismiss) }
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
                            palettes = Palettes.palettes,
                            currentPalette = state.palette,
                            onTitleChange = {  },
                            onCanvasColorChange = { onEvent(WhiteboardEvent.CanvasColorChange(it)) },
                            onColorPaletteIconClick = {
                                onEvent(WhiteboardEvent.OnColorPaletteIconClick(it))
                            },
                            onPalettePicked = { onEvent(WhiteboardEvent.OnPalettePicked(it)) },
                            onPaletteAdded = {  },
                            onCloseIconClick = { isCommandPaletteOpen = false }
                        )
                    },
                ) {
                    DrawingCanvas(
                        modifier = Modifier.fillMaxSize()
                            .pointerInput(Unit) {
                                awaitPointerEventScope {
                                    while (true) {
                                        val down = awaitFirstDown(requireUnconsumed = false)
                                        onEvent(WhiteboardEvent.OnCardClose)
                                        onEvent(WhiteboardEvent.OnStrokeWidthSliderClose)
                                        down.consume()
                                    }
                                }},
                        state = state,
                        onEvent = onEvent
                    )
                    CommandBarHorizontal(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(20.dp),
                        backgroundColor = state.canvasColor,
                        onHomeIconClick = {
                            onEvent(WhiteboardEvent.SaveMiniature(scope))
                            onHomeIconClick.invoke()
                        },
                        onMenuIconClick = { scope.launch { drawerState.open() } },
                        onSaveIconClick = { onEvent(WhiteboardEvent.SavePicture(scope)) },
                        onUndoIconClick = { onEvent(WhiteboardEvent.Undo) },
                        onRedoIconClick = { onEvent(WhiteboardEvent.Redo) }
                    )
                }
            }

            else -> {
                DrawingCanvas(
                    modifier = Modifier.fillMaxSize()
                        .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                onEvent(WhiteboardEvent.OnCardClose)
                                onEvent(WhiteboardEvent.OnStrokeWidthSliderClose)
                                isCommandPaletteOpen = false
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
                        onMenuIconClick = { isCommandPaletteOpen = !isCommandPaletteOpen },
                        onSaveIconClick = { onEvent(WhiteboardEvent.SavePicture(scope)) },
                        onUndoIconClick = { onEvent(WhiteboardEvent.Undo) },
                        onRedoIconClick = { onEvent(WhiteboardEvent.Redo) },
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    CommandPaletteCard(
                        isVisible = isCommandPaletteOpen,
                        title = state.whiteboardName,
                        canvasColors = state.preferredCanvasColors,
                        selectedCanvasColor = state.canvasColor,
                        palettes = Palettes.palettes,
                        currentPalette = state.palette,
                        onTitleChange = {  },
                        onCanvasColorChange = { onEvent(WhiteboardEvent.CanvasColorChange(it)) },
                        onColorPaletteIconClick = {
                            onEvent(WhiteboardEvent.OnColorPaletteIconClick(it))
                        },
                        onPalettePicked = { onEvent(WhiteboardEvent.OnPalettePicked(it)) },
                        onPaletteAdded = {  },
                        onCloseIconClick = { isCommandPaletteOpen = false }
                    )
                }
                DrawingToolBar(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp),
                    backgroundColor = state.canvasColor,
                    drawingToolVisibility = state.drawingToolVisibility,
                    currentDrawingTool = state.selectedDrawingTool,
                    onDrawingToolClick = { drawingTool: DrawingTool ->
                        onEvent(WhiteboardEvent.OnDrawingToolSelected(drawingTool))
                        onEvent(WhiteboardEvent.OnStrokeWidthSliderClose)
                        onEvent(WhiteboardEvent.OnCardClose)
                        isCommandPaletteOpen = false
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
                    colorDeletionMode = state.colorDeletionMode,
                    onSetColorDeletionMode = { mode: Boolean -> onEvent(WhiteboardEvent.SetColorDeletionMode(mode))},
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
                            markerColors = state.markerColors,
                            selectedMarker = state.selectedMarker,
                            selectedDrawingTool = state.selectedDrawingTool,
                            drawingToolVisibility = state.drawingToolVisibility,
                            onClick = { newColor: Color ->
                                onEvent(WhiteboardEvent.StrokeColorChange(newColor, false))
                                onEvent(WhiteboardEvent.OnStrokeWidthSliderClose)
                                isCommandPaletteOpen = false
                            },
                            onEraserClick = { eraserType: DrawingTool -> onEvent(WhiteboardEvent.OnDrawingToolSelected(eraserType)) }
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
                            modifier = Modifier.height(60.dp),
                            minButtonSize = 12.dp,
                            maxButtonSize = 40.dp,
                            strokeWidthList = state.strokeWidthList,
                            activeButton = state.activeStrokeWidthButton,
                            canvasColor = state.canvasColor,
                            onClick = { strokeNum: Int ->
                                onEvent(WhiteboardEvent.StrokeWidthButtonClicked(strokeNum))
                                onEvent(WhiteboardEvent.OnCardClose)
                                isCommandPaletteOpen = false
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
                detectDragGestures(
                    onDragStart = { offset ->
                        onEvent(WhiteboardEvent.StartDrawing(offset))
                        println("Draggin start")
                    },
                    onDrag = { change, _ ->
                        val offset = Offset(x = change.position.x, y = change.position.y)
                        onEvent(WhiteboardEvent.ContinueDrawing(offset))
                        println("Draggin big time")
                    },
                    onDragEnd = {
                        onEvent(WhiteboardEvent.FinishDrawing)
                    }
                )
            }
    ) {
        state.paths.forEach { path ->
            drawCustomPath(path)
        }

        state.currentPath?.let { path ->
            drawCustomPath(path)
        }
    }
    AnimateLaserPath(
        laserPenPath = state.laserPenPath,
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
                style = Stroke(width = path.strokeWidth.dp.toPx())
            )
        }

        else -> {
            drawPath(
                path = path.path,
                color = path.fillColor.copy(alpha = pathOpacity),
                style = Fill
            )
        }
    }
}

@Composable
fun AnimateLaserPath(
    laserPenPath: DrawnPath?,
    onPathAnimationComplete: () -> Unit
) {
    val animationProgress = remember { Animatable(initialValue = 1f) }
    LaunchedEffect(laserPenPath) {
        laserPenPath?.let {
            animationProgress.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 1000)
            )
            onPathAnimationComplete()
            animationProgress.snapTo(targetValue = 1f)
        }
    }
    val trimmedPath = Path()
    PathMeasure().apply {
        setPath(path = laserPenPath?.path, forceClosed = false)
        getSegment(
            startDistance = length * (1 - animationProgress.value),
            stopDistance = length,
            destination = trimmedPath //stores the resulting path segment in trimmedPath.
        )
    }
    Canvas(modifier = Modifier.fillMaxSize()) {
        laserPenPath?.let {
            drawPath(
                path = trimmedPath,
                color = laserPenPath.strokeColor,
                style = Stroke(width = laserPenPath.strokeWidth.dp.toPx())
            )
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


