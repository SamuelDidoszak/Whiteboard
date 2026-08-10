package org.samis.whiteboard.presentation.whiteboard.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.util.Locale
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.ln

@Composable
fun ZoomSliderCard(
    modifier: Modifier = Modifier,
    width: Dp = 256.dp,
    isVisible: Boolean,
    zoomSliderValue: Float,
    onZoomSliderValueChange: (Float) -> Unit,
    onCloseIconClick: () -> Unit
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        ElevatedCard(
            modifier = modifier.width(width),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
        ) {
            ZoomSliderContent(
                zoomSliderValue = zoomSliderValue,
                onZoomSliderValueChange = onZoomSliderValueChange,
                onCloseIconClick = onCloseIconClick
            )
        }
    }
}

@Composable
private fun ZoomSliderContent(
    modifier: Modifier = Modifier,
    zoomSliderValue: Float,
    onZoomSliderValueChange: (Float) -> Unit,
    onCloseIconClick: () -> Unit
) {
    Box(
        modifier = modifier.padding(10.dp)
    ) {
        SliderSection(
            sectionTitle = "Zoom level",
            showCloseButton = true,
            sliderValueRange = 0f..1f,
            sliderValue = scaleToSlider(zoomSliderValue),
            displayValue = String.format(Locale.US, "%.2fx", zoomSliderValue),
            onSliderValueChange = {
                val snapped = if (abs(it - 0.5f) < 0.015f) 0.5f else it
                onZoomSliderValueChange(sliderToScale(snapped))
            },
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
    displayValue: String,
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
                        contentDescription = "Close Zoom Slider"
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
            Text(text = displayValue)
        }
    }
}

private fun sliderToScale(sliderValue: Float): Float {
    val minLog = ln(1f / 12f)
    val maxLog = ln(12f)
    return exp(minLog + sliderValue * (maxLog - minLog))
}

private fun scaleToSlider(scale: Float): Float {
    val minLog = ln(1f / 12f)
    val maxLog = ln(12f)
    return (ln(scale) - minLog) / (maxLog - minLog)
}