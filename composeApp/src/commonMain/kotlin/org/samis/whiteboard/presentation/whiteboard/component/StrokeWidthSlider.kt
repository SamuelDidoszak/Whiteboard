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
import androidx.compose.foundation.layout.size
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
            modifier = modifier.width(256.dp)
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
            sectionTitle = "Stroke Width",
            showCloseButton = true,
            sliderValueRange = 1f..26f,
            sliderValue = strokeWidthSliderValue,
            onSliderValueChange = onStrokeWidthSliderValueChange,
            onCloseIconClick = onCloseIconClick
        )
        if (!showOpacity)
            return
        Spacer(modifier = Modifier.height(15.dp))
        SliderSection(
            sectionTitle = "Opacity",
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
                        .size(20.dp)
                        .padding(0.dp)
                ) {
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
                modifier = Modifier.weight(1f).height(25.dp),
                value = sliderValue,
                onValueChange = onSliderValueChange,
                valueRange = sliderValueRange
            )
            Text(text = String.format("%.1f", sliderValue))
        }
    }
}