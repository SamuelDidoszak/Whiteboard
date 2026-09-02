package org.samis.whiteboard.presentation.whiteboard.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.samis.whiteboard.presentation.theme.DarkGreen
import org.samis.whiteboard.presentation.theme.DisabledDark
import org.samis.whiteboard.presentation.theme.DisabledLight
import org.samis.whiteboard.presentation.theme.LightGreen

@Composable
fun ElevatedIconButton(
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    size: Dp = 40.dp,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDoubleClick: (() -> Unit)? = null,
    isDisabled: Boolean = false,
    icon: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val elevation = if (isPressed || isDisabled) 1.dp else 3.dp

    Surface(
        shape = CircleShape,
        shadowElevation = elevation,
        tonalElevation = elevation,
        color = backgroundColor,
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, getBorderColor(isSelected, backgroundColor, isDisabled)),
        modifier = modifier.size(size)
            .then(
                if (isDisabled) Modifier
                else if (onDoubleClick != null) {
                    Modifier.combinedClickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onDoubleClick = onDoubleClick,
                        onClick = onClick
                    )
                } else {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                }
            )
    ) {
        CompositionLocalProvider(LocalContentColor provides getButtonContentColor(backgroundColor, isDisabled)) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }
        }
    }
}

private fun getButtonContentColor(backgroundColor: Color, isDisabled: Boolean): Color {
    val isBackgroundLight = backgroundColor.luminance() > 0.5
    return if (!isDisabled) {
        if (isBackgroundLight) Color.Black else Color.White
    } else {
        if (isBackgroundLight) DisabledDark else DisabledLight
    }
}

private fun getBorderColor(isSelected: Boolean, backgroundColor: Color, isDisabled: Boolean): Color {
    return if (isSelected) {
        if (backgroundColor.luminance() > 0.5)
            DarkGreen
        else
            LightGreen
    } else getButtonContentColor(backgroundColor, isDisabled)
}