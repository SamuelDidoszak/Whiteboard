package org.samis.whiteboard.presentation.util

import androidx.compose.ui.graphics.Color

data class Palette(
    val background: Color,
    val foreground: Color,
    val red: Color,
    val blue: Color,
    val green: Color,
    val others: List<Color> = listOf()
) {
    val colorList = listOf(background, foreground, red, blue, green) + others
}