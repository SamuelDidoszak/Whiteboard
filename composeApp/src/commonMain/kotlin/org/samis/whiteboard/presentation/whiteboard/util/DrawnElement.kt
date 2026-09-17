package org.samis.whiteboard.presentation.whiteboard.util

import org.samis.whiteboard.domain.model.DrawnPath

sealed class DrawnElement {
    abstract var id: Long?

    data class Path(val path: DrawnPath): DrawnElement() {
        override var id: Long?
            get() = path.id
            set(value) { path.id = value }
    }

    data class Picture(val picture: AddedPicture): DrawnElement() {
        override var id: Long?
            get() = picture.id
            set(value) { picture.id = value }
    }
}