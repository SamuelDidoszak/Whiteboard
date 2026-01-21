package org.samis.whiteboard.domain.model

import androidx.compose.ui.graphics.Color
import kotlinx.datetime.Instant
import org.samis.whiteboard.presentation.theme.Palettes
import org.samis.whiteboard.presentation.util.Palette

data class Whiteboard(
    val id: Long? = null,
    val name: String,
    val createTime: Instant,
    val lastModified: Instant,
    val palette: Palette = Palettes.defaultPalettes.first(),
    val markerColors: List<Color> = listOf(palette.foreground, palette.red, palette.blue, palette.green),
    val strokeWidths: List<Float> = listOf(1.8f, 5f, 10f),
    val activeStrokeWidthButton: Int = 1,
    val opacity: Float = 100f,
    val fillColor: Color = Color.Transparent,
    val pointer: Int? = null,
    val miniatureSrc: String? = null
)
