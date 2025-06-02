package org.samis.whiteboard.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import java.io.File

expect fun capture(
    scope: CoroutineScope,
    controller: Any,
    contextProvider: IContextProvider,
    fileName: String,
    miniature: Boolean,
    miniaturePath: String?,
    onFileSave: (file: File) -> Unit
)

@Composable
expect fun rememberCaptureController(): Any

expect fun Modifier.capturable(
    controller: Any?
): Modifier