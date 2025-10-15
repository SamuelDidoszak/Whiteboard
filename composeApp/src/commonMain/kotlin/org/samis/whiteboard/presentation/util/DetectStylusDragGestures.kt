package org.samis.whiteboard.presentation.util

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.PointerType
import kotlinx.coroutines.coroutineScope
import kotlin.coroutines.cancellation.CancellationException

suspend fun PointerInputScope.detectStylusDragGestures(
    stylusInput: Boolean,
    onDragStart: (Offset) -> Unit = {},
    onDrag: (PointerInputChange, Offset) -> Unit = { _, _ -> },
    onDragEnd: () -> Unit = {},
    onDragCancel: () -> Unit = {}
) = coroutineScope {
    awaitEachGesture {
        // Wait for first pointer down
        val down = awaitFirstDown(requireUnconsumed = false)

        // Only proceed if it's a stylus
        if (stylusInput && down.type != PointerType.Stylus) {
            // Consume and ignore non-stylus
            down.consume()
            return@awaitEachGesture
        }

        onDragStart(down.position)

        var pointer = down

        // Track drag events
        try {
            drag(pointer.id) { change ->
                onDrag(change, change.position)
                change.consume()
            }
            onDragEnd()
        } catch (e: CancellationException) {
            onDragCancel()
        }
    }
}