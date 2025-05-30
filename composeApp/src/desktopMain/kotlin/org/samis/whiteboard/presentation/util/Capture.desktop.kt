package org.samis.whiteboard.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope

actual fun capture(
    scope: CoroutineScope,
    controller: Any,
    contextProvider: IContextProvider,
    folderName: String,
    fileName: String
) {
    TODO("Not yet implemented")
}

@Composable
actual fun rememberCaptureController(): Any {
    TODO("Not yet implemented")
}

actual fun Modifier.capturable(controller: Any?): Modifier {
    TODO("Not yet implemented")
}