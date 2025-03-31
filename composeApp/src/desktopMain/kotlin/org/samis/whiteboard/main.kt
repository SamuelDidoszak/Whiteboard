package org.samis.whiteboard

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.samis.whiteboard.di.initKoin
import org.samis.whiteboard.presentation.App

fun main() {
    initKoin()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Whiteboard",
        ) {
            App()
        }
    }
}