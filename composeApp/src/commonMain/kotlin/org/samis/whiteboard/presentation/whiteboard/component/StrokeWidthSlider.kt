package org.samis.whiteboard.presentation.whiteboard.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun StrokeWidthSliderDrawer(
    modifier: Modifier = Modifier,
    showOpacity: Boolean,
    strokeWidthSliderValue: Float,
    onStrokeWidthSliderValueChange: (Float) -> Unit,
    opacitySliderValue: Float,
    onOpacitySliderValueChange: (Float) -> Unit,
    onCloseIconClick: () -> Unit
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth(0.8f)
            .fillMaxHeight()
    ) {
        StrokeWidthSliderContent(
            showOpacity = showOpacity,
            strokeWidthSliderValue = strokeWidthSliderValue,
            onStrokeWidthSliderValueChange = onStrokeWidthSliderValueChange,
            opacitySliderValue = opacitySliderValue,
            onOpacitySliderValueChange = onOpacitySliderValueChange,
            onCloseIconClick = onCloseIconClick
        )
    }
}

@Composable
fun StrokeWidthSliderCard(
    modifier: Modifier = Modifier,
    isVisible: Boolean,
    showOpacity: Boolean,
    strokeWidthSliderValue: Float,
    onStrokeWidthSliderValueChange: (Float) -> Unit,
    opacitySliderValue: Float,
    onOpacitySliderValueChange: (Float) -> Unit,
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
            StrokeWidthSliderContent(
                showOpacity = showOpacity,
                strokeWidthSliderValue = strokeWidthSliderValue,
                onStrokeWidthSliderValueChange = onStrokeWidthSliderValueChange,
                opacitySliderValue = opacitySliderValue,
                onOpacitySliderValueChange = onOpacitySliderValueChange,
                onCloseIconClick = onCloseIconClick
            )
        }
    }
}

@Composable
private fun StrokeWidthSliderContent(
    modifier: Modifier = Modifier,
    showOpacity: Boolean,
    showCloseButton: Boolean = false,
    strokeWidthSliderValue: Float,
    onStrokeWidthSliderValueChange: (Float) -> Unit,
    opacitySliderValue: Float,
    onOpacitySliderValueChange: (Float) -> Unit,
    onCloseIconClick: () -> Unit
) {
    Column(
        modifier = modifier.padding(10.dp)
    ) {
        SliderSection(
            sectionTitle = if (showOpacity) "Stroke Width" else "",
            showCloseButton = showCloseButton,
            sliderValueRange = 1f..25f,
            sliderValue = strokeWidthSliderValue,
            onSliderValueChange = onStrokeWidthSliderValueChange,
            onCloseIconClick = onCloseIconClick
        )
        Spacer(modifier = Modifier.height(15.dp))
        SliderSection(
            sectionTitle = if (showOpacity) "Opacity" else "",
            showCloseButton = false,
            sliderValueRange = 1f..100f,
            sliderValue = opacitySliderValue,
            onSliderValueChange = onOpacitySliderValueChange,
            onCloseIconClick = onCloseIconClick
        )
    }
}

@Composable
private fun SliderSection(
    modifier: Modifier = Modifier,
    sectionTitle: String,
    showCloseButton: Boolean,
    sliderValue: Float,
    onSliderValueChange: (Float) -> Unit,
    sliderValueRange: ClosedFloatingPointRange<Float>,
    onCloseIconClick: () -> Unit
) {
    Column(modifier = modifier) {
        Row(horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = sectionTitle,
                style = MaterialTheme.typography.titleSmall
            )
            if (showCloseButton) {
                IconButton(onClick = onCloseIconClick) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close ${sectionTitle.replaceFirstChar { it.uppercaseChar() }} Slider"
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(5.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Slider(
                modifier = Modifier.weight(1f),
                value = sliderValue,
                onValueChange = onSliderValueChange,
                valueRange = sliderValueRange
            )
            Text(text = "${sliderValue.toInt()}")
        }
    }
}