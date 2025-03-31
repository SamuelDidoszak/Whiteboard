package org.samis.whiteboard.presentation.whiteboard

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.samis.whiteboard.domain.model.DrawnPath
import org.samis.whiteboard.presentation.util.UiType
import org.samis.whiteboard.presentation.util.getUiType
import org.samis.whiteboard.presentation.util.rememberScreenSizeSize
import org.samis.whiteboard.presentation.whiteboard.component.ColorSelectionDialog
import org.samis.whiteboard.presentation.whiteboard.component.CommandPaletteCard
import org.samis.whiteboard.presentation.whiteboard.component.CommandPaletteDrawerContent
import org.samis.whiteboard.presentation.whiteboard.component.DrawingToolFAB
import org.samis.whiteboard.presentation.whiteboard.component.DrawingToolsCardHorizontal
import org.samis.whiteboard.presentation.whiteboard.component.DrawingToolsCardVertical
import org.samis.whiteboard.presentation.whiteboard.component.TopBarHorizontal
import org.samis.whiteboard.presentation.whiteboard.component.TopBarVertical

@Composable
fun WhiteboardScreen(
    modifier: Modifier = Modifier,
    state: WhiteboardState,
    onEvent: (WhiteboardEvent) -> Unit,
    onHomeIconClick: () -> Unit
) {

    val screenSize = rememberScreenSizeSize()
    val uiType = screenSize.getUiType()

    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    var isCommandPaletteOpen by rememberSaveable { mutableStateOf(false) }

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
                            onCloseIconClick = { scope.launch { drawerState.close() } },
                            selectedDrawingTool = state.selectedDrawingTool,
                            selectedCanvasColor = state.canvasColor,
                            selectedStrokeColor = state.strokeColor,
                            selectedFillColor = state.fillColor,
                            canvasColors = state.preferredCanvasColors,
                            strokeColors = state.preferredStrokeColors,
                            fillColors = state.preferredFillColors,
                            onCanvasColorChange = { onEvent(WhiteboardEvent.CanvasColorChange(it)) },
                            onStrokeColorChange = { onEvent(WhiteboardEvent.StrokeColorChange(it)) },
                            onFillColorChange = { onEvent(WhiteboardEvent.FillColorChange(it)) },
                            strokeWidthSliderValue = state.strokeWidth,
                            opacitySliderValue = state.opacity,
                            onStrokeWidthSliderValueChange = {
                                onEvent(WhiteboardEvent.StrokeSliderValueChange(it))
                            },
                            onOpacitySliderValueChange = {
                                onEvent(WhiteboardEvent.OpacitySliderValueChange(it))
                            },
                            onColorPaletteIconClick = {
                                onEvent(WhiteboardEvent.OnColorPaletteIconClick(it))
                            }
                        )
                    },
                ) {
                    DrawingCanvas(
                        modifier = Modifier.fillMaxSize(),
                        state = state,
                        onEvent = onEvent
                    )
                    TopBarHorizontal(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(20.dp),
                        onHomeIconClick = onHomeIconClick,
                        onUndoIconClick = {},
                        onRedoIconClick = {},
                        onMenuIconClick = { scope.launch { drawerState.open() } }
                    )
                    DrawingToolFAB(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(20.dp),
                        isVisible = !state.isDrawingToolsCardVisible,
                        selectedTool = state.selectedDrawingTool,
                        onClick = { onEvent(WhiteboardEvent.OnFABClick) }
                    )
                    DrawingToolsCardHorizontal(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(20.dp),
                        isVisible = state.isDrawingToolsCardVisible,
                        selectedTool = state.selectedDrawingTool,
                        onToolClick = { onEvent(WhiteboardEvent.OnDrawingToolSelected(it)) },
                        onCloseIconClick = { onEvent(WhiteboardEvent.OnDrawingToolsCardClose) }
                    )
                }
            }

            else -> {
                println("Not compact")
                DrawingCanvas(
                    modifier = Modifier.fillMaxSize(),
                    state = state,
                    onEvent = onEvent
                )
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .align(Alignment.TopStart)
                        .padding(20.dp)
                ) {
                    TopBarVertical(
                        onHomeIconClick = onHomeIconClick,
                        onUndoIconClick = {},
                        onRedoIconClick = {},
                        onMenuIconClick = { isCommandPaletteOpen != isCommandPaletteOpen }
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    CommandPaletteCard(
                        isVisible = isCommandPaletteOpen,
                        onCloseIconClick = { isCommandPaletteOpen = false },
                        selectedDrawingTool = state.selectedDrawingTool,
                        selectedCanvasColor = state.canvasColor,
                        selectedStrokeColor = state.strokeColor,
                        selectedFillColor = state.fillColor,
                        canvasColors = state.preferredCanvasColors,
                        strokeColors = state.preferredStrokeColors,
                        fillColors = state.preferredFillColors,
                        onCanvasColorChange = { onEvent(WhiteboardEvent.CanvasColorChange(it)) },
                        onStrokeColorChange = { onEvent(WhiteboardEvent.StrokeColorChange(it)) },
                        onFillColorChange = { onEvent(WhiteboardEvent.FillColorChange(it)) },
                        strokeWidthSliderValue = state.strokeWidth,
                        opacitySliderValue = state.opacity,
                        onStrokeWidthSliderValueChange = {
                            onEvent(WhiteboardEvent.StrokeSliderValueChange(it))
                        },
                        onOpacitySliderValueChange = {
                            onEvent(WhiteboardEvent.OpacitySliderValueChange(it))
                        },
                        onColorPaletteIconClick = {
                            onEvent(WhiteboardEvent.OnColorPaletteIconClick(it))
                        }
                    )
                }
                DrawingToolFAB(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(20.dp),
                    isVisible = !state.isDrawingToolsCardVisible,
                    selectedTool = state.selectedDrawingTool,
                    onClick = { onEvent(WhiteboardEvent.OnFABClick) }
                )
                DrawingToolsCardVertical(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(20.dp),
                    isVisible = state.isDrawingToolsCardVisible,
                    selectedTool = state.selectedDrawingTool,
                    onToolClick = { onEvent(WhiteboardEvent.OnDrawingToolSelected(it)) },
                    onCloseIconClick = { onEvent(WhiteboardEvent.OnDrawingToolsCardClose) }
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
            .background(state.canvasColor)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        onEvent(WhiteboardEvent.StartDrawing(offset))
                    },
                    onDrag = { change, _ ->
                        val offset = Offset(x = change.position.x, y = change.position.y)
                        onEvent(WhiteboardEvent.ContinueDrawing(offset))
                    },
                    // Here?
                    onDragEnd = {
                        onEvent(WhiteboardEvent.FinishDrawing)
                    }
                )
            }
    ) {
        // Hereby
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
    // Hereby2
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






