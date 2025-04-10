package org.samis.whiteboard.presentation.whiteboard.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.samis.whiteboard.domain.model.ColorPaletteType
import org.samis.whiteboard.domain.model.DrawingTool
import whiteboard.composeapp.generated.resources.Res
import whiteboard.composeapp.generated.resources.ic_transparent_bg
import whiteboard.composeapp.generated.resources.img_color_wheel

@Composable
fun ColorPickerDrawer(
    modifier: Modifier = Modifier,
    selectedDrawingTool: DrawingTool,
    strokeColors: List<Color>,
    selectedStrokeColor: Color,
    onStrokeColorChange: (Color) -> Unit,
    fillColors: List<Color>,
    selectedFillColor: Color,
    onFillColorChange: (Color) -> Unit,
    onColorPaletteIconClick: (ColorPaletteType) -> Unit,
    onCloseIconClick: () -> Unit
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth(0.8f)
            .fillMaxHeight()
    ) {
        ColorPickerContent(
            selectedDrawingTool = selectedDrawingTool,
            strokeColors = strokeColors,
            selectedStrokeColor = selectedStrokeColor,
            onStrokeColorChange = onStrokeColorChange,
            fillColors = fillColors,
            selectedFillColor = selectedFillColor,
            onFillColorChange = onFillColorChange,
            onColorPaletteIconClick = onColorPaletteIconClick,
            onCloseIconClick = onCloseIconClick
        )
    }
}

@Composable
fun ColorPickerCard(
    modifier: Modifier = Modifier,
    isVisible: Boolean,
    selectedDrawingTool: DrawingTool,
    strokeColors: List<Color>,
    selectedStrokeColor: Color,
    onStrokeColorChange: (Color) -> Unit,
    fillColors: List<Color>,
    selectedFillColor: Color,
    onFillColorChange: (Color) -> Unit,
    onColorPaletteIconClick: (ColorPaletteType) -> Unit,
    onCloseIconClick: () -> Unit
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        ElevatedCard(
            modifier = modifier.width(250.dp)
        ) {
            ColorPickerContent(
                selectedDrawingTool = selectedDrawingTool,
                strokeColors = strokeColors,
                selectedStrokeColor = selectedStrokeColor,
                onStrokeColorChange = onStrokeColorChange,
                fillColors = fillColors,
                selectedFillColor = selectedFillColor,
                onFillColorChange = onFillColorChange,
                onColorPaletteIconClick = onColorPaletteIconClick,
                onCloseIconClick = onCloseIconClick
            )
        }
    }
}

@Composable
private fun ColorPickerContent(
    modifier: Modifier = Modifier,
    selectedDrawingTool: DrawingTool,
    strokeColors: List<Color>,
    selectedStrokeColor: Color,
    fillColors: List<Color>,
    selectedFillColor: Color,
    onFillColorChange: (Color) -> Unit,
    onStrokeColorChange: (Color) -> Unit,
    onColorPaletteIconClick: (ColorPaletteType) -> Unit,
    onCloseIconClick: () -> Unit
) {
    Column(
        modifier = modifier.padding(6.dp)
    ) {
        ColorSection(
            sectionTitle = "Stroke",
            showCloseButton = true,
            colors = strokeColors,
            selectedColor = selectedStrokeColor,
            onColorChange = onStrokeColorChange,
            onColorPaletteClick = { onColorPaletteIconClick(ColorPaletteType.MARKER) },
            onCloseIconClick = onCloseIconClick
        )
        if (selectedDrawingTool.isFillable()) {
            Spacer(modifier = Modifier.height(12.dp))
            ColorSection(
                sectionTitle = "Fill",
                showCloseButton = false,
                isFillColorsSection = true,
                colors = fillColors,
                selectedColor = selectedFillColor,
                onColorChange = onFillColorChange,
                onColorPaletteClick = { onColorPaletteIconClick(ColorPaletteType.FILL) },
                onCloseIconClick = onCloseIconClick
            )
        } else Unit
    }
}

@Composable
private fun ColorSection(
    sectionTitle: String,
    showCloseButton: Boolean,
    isFillColorsSection: Boolean = false,
    colors: List<Color>,
    selectedColor: Color,
    onColorChange: (Color) -> Unit,
    onColorPaletteClick: () -> Unit,
    onCloseIconClick: () -> Unit
) {
    Column {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = sectionTitle,
                style = MaterialTheme.typography.titleSmall
            )
            if (showCloseButton) {
                IconButton(
                    onClick = onCloseIconClick,
                    modifier = Modifier
                        .align(Alignment.Top)
                        .size(25.dp)
                        .padding(0.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close ${sectionTitle.replaceFirstChar { it.uppercaseChar() }} Color Picker"
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(5.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isFillColorsSection) {
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
                val index = colors.indexOf(color)
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
                        .clickable { onColorChange(color) }
                )
            }
            item {
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