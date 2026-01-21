package org.samis.whiteboard.presentation.theme

import androidx.compose.ui.graphics.Color
import org.samis.whiteboard.presentation.util.Palette

object Palettes {

    val defaultPalettes = listOf(
        Palette(
            background = Color.White,
            foreground = Color.Black,
            red = Color.Red,
            blue = Color.Blue,
            green = Color.Green
        ),
        Palette(
            background = Color.White,
            foreground = Color.Black,
            red = Red40,
            blue = Blue40,
            green = Green40
        ),
//        Palette(
//            background = Color(0xFF1c1c1c),
//            foreground = Color(0xFF),
//            red = Color(0xFF),
//            blue = Color(0xFF),
//            green = Color(0xFF)
//        ),
        Palette(
            background = Color(0xFF101010),
            foreground = Color(0xFFdedede),
            red = Color(0xFFc62a2a),
            blue = Color(0xFF0c6dcf),
            green = Color(0xFF308234)
        ),
        Palette(
            background = Color(0xFF000000),
            foreground = Color(0xFFe8e8e8),
            red = Color(0xFFd32f2f),
            blue = Color(0xFF1976d2),
            green = Color(0xFF388e3c)
        ),
    )
}