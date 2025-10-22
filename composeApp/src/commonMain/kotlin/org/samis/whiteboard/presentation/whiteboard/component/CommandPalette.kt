package org.samis.whiteboard.presentation.whiteboard.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.samis.whiteboard.domain.model.ColorPaletteType
import org.samis.whiteboard.presentation.util.Palette
import whiteboard.composeapp.generated.resources.Res
import whiteboard.composeapp.generated.resources.ic_transparent_bg
import whiteboard.composeapp.generated.resources.img_color_wheel

@Composable
fun CommandPaletteDrawerContent(
    modifier: Modifier = Modifier,
    title: String,
    canvasColors: List<Color>,
    selectedCanvasColor: Color,
    palettes: List<Palette>,
    currentPalette: Palette,
    onTitleChange: (String) -> Unit,
    onCanvasColorChange: (Color) -> Unit,
    onColorPaletteIconClick: (ColorPaletteType) -> Unit,
    onPalettePicked: (Palette) -> Unit,
    onPaletteAdded: () -> Unit,
    onCloseIconClick: () -> Unit,
    colorDeletionMode: Boolean,
    onSetColorDeletionMode: (Boolean) -> Unit,
    onColorDeleted: (Color, ColorPaletteType) -> Unit,
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth(0.8f)
            .fillMaxHeight()
    ) {
        CommandPaletteContent(
            title = title,
            canvasColors = canvasColors,
            selectedCanvasColor = selectedCanvasColor,
            palettes = palettes,
            currentPalette = currentPalette,
            onTitleChange = onTitleChange,
            onCanvasColorChange = onCanvasColorChange,
            onColorPaletteIconClick = onColorPaletteIconClick,
            onPalettePicked = onPalettePicked,
            onPaletteAdded = onPaletteAdded,
            onCloseIconClick = onCloseIconClick,
            colorDeletionMode = colorDeletionMode,
            onSetColorDeletionMode = onSetColorDeletionMode,
            onColorDeleted = onColorDeleted
        )
    }
}

@Composable
fun CommandPaletteCard(
    modifier: Modifier = Modifier,
    isVisible: Boolean,
    title: String,
    canvasColors: List<Color>,
    selectedCanvasColor: Color,
    palettes: List<Palette>,
    currentPalette: Palette,
    onTitleChange: (String) -> Unit,
    onCanvasColorChange: (Color) -> Unit,
    onColorPaletteIconClick: (ColorPaletteType) -> Unit,
    onPalettePicked: (Palette) -> Unit,
    onPaletteAdded: () -> Unit,
    onCloseIconClick: () -> Unit,
    colorDeletionMode: Boolean,
    onSetColorDeletionMode: (Boolean) -> Unit,
    onColorDeleted: (Color, ColorPaletteType) -> Unit,
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = isVisible,
        enter = slideInHorizontally() + fadeIn(tween(100)),
        exit = slideOutHorizontally(animationSpec = tween(500)) + fadeOut(tween(350))
    ) {
        ElevatedCard(
            modifier = modifier.width(250.dp).height(350.dp),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
        ) {
            CommandPaletteContent(
                title = title,
                canvasColors = canvasColors,
                selectedCanvasColor = selectedCanvasColor,
                palettes = palettes,
                currentPalette = currentPalette,
                onTitleChange = onTitleChange,
                onCanvasColorChange = onCanvasColorChange,
                onColorPaletteIconClick = onColorPaletteIconClick,
                onPalettePicked = onPalettePicked,
                onPaletteAdded = onPaletteAdded,
                onCloseIconClick = onCloseIconClick,
                colorDeletionMode = colorDeletionMode,
                onSetColorDeletionMode = onSetColorDeletionMode,
                onColorDeleted = onColorDeleted
            )
        }
    }
}

@Composable
private fun CommandPaletteContent(
    modifier: Modifier = Modifier,
    title: String,
    canvasColors: List<Color>,
    selectedCanvasColor: Color,
    palettes: List<Palette>,
    currentPalette: Palette,
    onTitleChange: (String) -> Unit,
    onCanvasColorChange: (Color) -> Unit,
    onColorPaletteIconClick: (ColorPaletteType) -> Unit,
    onPalettePicked: (Palette) -> Unit,
    onPaletteAdded: () -> Unit,
    onCloseIconClick: () -> Unit,
    colorDeletionMode: Boolean,
    onSetColorDeletionMode: (Boolean) -> Unit,
    onColorDeleted: (Color, ColorPaletteType) -> Unit,
) {
    val updatedCanvasColors = listOf(Color.White) + canvasColors

    Column(
        modifier = modifier.padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
            IconButton(onClick = onCloseIconClick) {
                Icon(Icons.Default.Close, contentDescription = "Close Command Palette")
            }
        }
        HorizontalDivider(modifier = Modifier.height(20.dp))
        ColorSection(
            sectionTitle = "Canvas",
            colors = updatedCanvasColors,
            selectedColor = selectedCanvasColor,
            onColorChange = onCanvasColorChange,
            onColorPaletteClick = { onColorPaletteIconClick(ColorPaletteType.CANVAS) },
            colorDeletionMode = colorDeletionMode,
            onSetColorDeletionMode = onSetColorDeletionMode,
            onColorDeleted = onColorDeleted
        )
        Spacer(modifier = Modifier.height(20.dp))
        PaletteSection(
            palettes = palettes,
            currentPalette = currentPalette,
            onPalettePicked = onPalettePicked,
            onPaletteAdded = onPaletteAdded
        )
//        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun PaletteSection(
    palettes: List<Palette>,
    currentPalette: Palette,
    onPalettePicked: (Palette) -> Unit,
    onPaletteAdded: () -> Unit
) {
    Column {
        Text(
            text = "Palettes",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier
                .fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(palettes) { palette ->
                val isActive = palette == currentPalette
                Surface(
                    tonalElevation = if (isActive) 0.dp else 2.dp,
                    shadowElevation = if (isActive) 0.dp else 2.dp,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = if (isActive) {
                                MaterialTheme.colorScheme.onSurface
                            } else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onPalettePicked(palette) }
                ) {
                    val customBrush = Brush.linearGradient(
                        colorStops = arrayOf(
                            0.0f to palette.background,
                            0.25f to palette.background,
                            0.25001f to palette.background,
                            1.0f to Color.Transparent
                        )
                    )

                    Box(
                        modifier = Modifier
                            .background(brush = customBrush)
                    ) {
                        PalettePreview(palette)
                    }
                }
            }
        }
    }
}

@Composable
private fun PalettePreview(
    palette: Palette
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(start = 4.dp, top = 3.dp, bottom = 3.dp, end = 4.dp)
    ) {
        items(palette.colorList.subList(1, palette.colorList.size)) { color ->
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .padding(2.dp)
                    .background(color, shape = CircleShape)
            )
        }
    }
}

@Composable
private fun ColorSection(
    sectionTitle: String,
    addTransparentColor: Boolean = false,
    colors: List<Color>,
    selectedColor: Color,
    onColorChange: (Color) -> Unit,
    onColorPaletteClick: () -> Unit,
    colorDeletionMode: Boolean,
    onSetColorDeletionMode: (Boolean) -> Unit,
    onColorDeleted: (Color, ColorPaletteType) -> Unit,
) {
    Column {
        Text(
            text = sectionTitle,
            style = MaterialTheme.typography.titleSmall
        )
        Spacer(modifier = Modifier.height(10.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (addTransparentColor) {
                item {
                    Icon(
                        modifier = Modifier
                            .size(30.dp)
                            .border(
                                width = 1.dp,
                                color = if (selectedColor == Color.Transparent) {
                                    MaterialTheme.colorScheme.primary
                                } else Color.Transparent,
                                shape = CircleShape
                            )
                            .padding(2.dp)
                            .clip(CircleShape)
                            .clickable { onColorChange(Color.Transparent) },
                        painter = painterResource(Res.drawable.ic_transparent_bg),
                        contentDescription = "Set transparent background",
                        tint = Color.Unspecified
                    )
                }
            }
            items(colors) { color ->
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .border(
                            width = 1.dp,
                            color = if (selectedColor == color) {
                                MaterialTheme.colorScheme.primary
                            } else Color.Transparent,
                            shape = CircleShape
                        )
                        .padding(2.dp)
                        .background(color, CircleShape)
                        .pointerInput(colorDeletionMode) {
                            detectTapGestures(
                                onTap = {
                                    if (!colorDeletionMode)
                                        onColorChange(color)
                                    else onColorDeleted(color, ColorPaletteType.CANVAS)
                                },
                                onLongPress = { onSetColorDeletionMode(!colorDeletionMode) }
                            )
                        }
                ) {
                    if (colorDeletionMode) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .background(Color.Red, CircleShape)
                                .align(Alignment.TopEnd)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Delete color",
                                tint = Color.White,
                                modifier = Modifier.size(10.dp).align(Alignment.Center)
                            )
                        }
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.width(5.dp))
                IconButton(
                    modifier = Modifier.size(25.dp),
                    onClick = { onColorPaletteClick() }
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.img_color_wheel),
                        contentDescription = "Set custom color",
                        tint = Color.Unspecified
                    )
                }
            }
        }
    }
}