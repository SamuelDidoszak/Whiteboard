package org.samis.whiteboard.presentation.util

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable

@Composable
actual fun registerBackHandler(onBack: () -> Unit) {
    BackHandler {
        onBack.invoke()
    }
}