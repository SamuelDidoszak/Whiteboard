package org.samis.whiteboard.data.mapper

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import org.samis.whiteboard.data.database.entity.PaletteEntity
import org.samis.whiteboard.presentation.util.Palette

fun Palette.toPaletteEntity(): PaletteEntity {
    return PaletteEntity(
        background = this.background.toArgb(),
        foreground = this.foreground.toArgb(),
        red = this.red.toArgb(),
        blue = this.blue.toArgb(),
        green = this.green.toArgb(),
        others = this.others.map { it.toArgb() }
    )
}

fun PaletteEntity.toPalette(): Palette {
    return Palette(
        background = Color(this.background),
        foreground = Color(this.foreground),
        red = Color(this.red),
        blue = Color(this.blue),
        green = Color(this.green),
        others = this.others.map { Color(it) }
    )
}