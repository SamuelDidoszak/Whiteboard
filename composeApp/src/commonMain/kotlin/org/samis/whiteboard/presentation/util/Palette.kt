package org.samis.whiteboard.presentation.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

data class Palette(
    val background: Color,
    val foreground: Color,
    val red: Color,
    val blue: Color,
    val green: Color,
    val others: List<Color> = listOf()
) {
    val colorList = listOf(background, foreground, red, blue, green) + others

    override fun equals(other: Any?): Boolean {
        if (other !is Palette)
            return false
        return background == other.background && foreground == other.foreground && red == other.red && blue == other.blue && green == other.green && others == other.others
    }

    override fun hashCode(): Int {
        var result = background.hashCode()
        result = 31 * result + foreground.hashCode()
        result = 31 * result + red.hashCode()
        result = 31 * result + blue.hashCode()
        result = 31 * result + green.hashCode()
        result = 31 * result + others.hashCode()
        result = 31 * result + colorList.hashCode()
        return result
    }

    override fun toString(): String {
        return "${background.toArgb()}|${foreground.toArgb()}|${red.toArgb()}|${blue.toArgb()}|${green.toArgb()}|${others.map { it.toArgb() }}"
    }

    companion object {
        fun fromString(paletteString: String): Palette {
            val (background, foreground, red, blue, green) = paletteString.split("|", limit = 6).take(5).map { Color(it.toInt()) }
            val othersString = paletteString.substringAfterLast("|[").substringBefore("]")
            val others = if (othersString.isNotEmpty()) othersString.split(", ").map { Color(it.toInt()) }.toList() else listOf()
            return Palette(background, foreground, red, blue, green, others)
        }
    }
}