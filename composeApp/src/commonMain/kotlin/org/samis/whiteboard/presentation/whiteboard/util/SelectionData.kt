package org.samis.whiteboard.presentation.whiteboard.util

import androidx.compose.ui.geometry.Offset

data class SelectionData(
    var previous: Offset? = null,
    var original: AddedPicture? = null
) {
    fun start(picture: AddedPicture, position: Offset) {
        previous = position
        original = picture.copy()
    }

    fun reset() {
        previous = null
        original = null
    }
}
