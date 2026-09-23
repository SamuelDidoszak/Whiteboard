package org.samis.whiteboard.presentation.whiteboard.util

import androidx.compose.ui.geometry.Offset

data class SelectionData(
    var previous: Offset? = null,
    var frame: SelectionFrame? = null,
    var original: List<DrawnElement> = emptyList(),
    var drawnIndices: List<Int> = emptyList(),
    var rotation: Float = 0f
) {
    fun start(elements: List<DrawnElement>, bounds: SelectionFrame, position: Offset, drawnElements: List<DrawnElement>) {
        previous = position
        frame = bounds
        original = elements.map { element ->
            when (element) {
                is DrawnElement.Picture -> DrawnElement.Picture(element.picture.copy())
                is DrawnElement.Path -> DrawnElement.Path(element.path.copy())
            }
        }
        drawnIndices = elements.map { element -> drawnElements.indexOf(element) }
        rotation = 0f
    }

    fun reset() {
        previous = null
        frame = null
        original = emptyList()
        drawnIndices = emptyList()
        rotation = 0f
    }
}
