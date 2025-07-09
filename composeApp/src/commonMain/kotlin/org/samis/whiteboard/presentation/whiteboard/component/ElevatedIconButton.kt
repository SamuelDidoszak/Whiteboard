package org.samis.whiteboard.presentation.whiteboard.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import org.samis.whiteboard.presentation.theme.DarkGreen
import org.samis.whiteboard.presentation.theme.LightGreen

@Composable
fun ElevatedIconButton(
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val elevation = if (isPressed) 1.dp else 3.dp

    Surface(
        shape = CircleShape,
        shadowElevation = elevation,
        tonalElevation = elevation,
        color = backgroundColor,
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, getBorderColor(isSelected, backgroundColor)),
        modifier = modifier.size(40.dp)
    ) {
        CompositionLocalProvider(LocalContentColor provides getButtonContentColor(backgroundColor)) {
            IconButton(
                onClick = onClick,
                modifier = Modifier.fillMaxSize(),
                interactionSource = interactionSource,
            ) {
                icon()
            }
        }
    }
}

private fun getButtonContentColor(backgroundColor: Color): Color {
    val isBackgroundLight = backgroundColor.luminance() > 0.5
    return if (isBackgroundLight) Color.Black else Color.White
}

private fun getBorderColor(isSelected: Boolean, backgroundColor: Color): Color {
    return if (isSelected) {
        if (backgroundColor.luminance() > 0.5)
            DarkGreen
        else
            LightGreen
    } else getButtonContentColor(backgroundColor)
}