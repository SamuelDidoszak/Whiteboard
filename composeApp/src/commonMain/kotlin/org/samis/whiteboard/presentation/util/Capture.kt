package org.samis.whiteboard.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope

expect fun capture(
    scope: CoroutineScope,
    controller: Any,
    contextProvider: IContextProvider,
    folderName: String,
    fileName: String
)

@Composable
expect fun rememberCaptureController(): Any

expect fun Modifier.capturable(
    controller: Any?
): Modifier