package org.samis.whiteboard.domain.model

import org.samis.whiteboard.presentation.whiteboard.util.AddedPicture

sealed class Update(
    open var id: Long? = null,
    open var whiteboardId: Long? = null
) {
    data class AddPath(
        override val path: DrawnPath,
        override var id: Long? = null,
        override var whiteboardId: Long? = null
    ) : Update(id, whiteboardId), HasPath

    data class RemovePath(
        override val path: DrawnPath,
        override var id: Long? = null,
        override var whiteboardId: Long? = null
    ) : Update(id, whiteboardId), HasPath

    data class Erase(
        override val path: DrawnPath,
        override var id: Long? = null,
        override var whiteboardId: Long? = null
    ) : Update(id, whiteboardId), HasPath

    data class RemoveErase(
        override val path: DrawnPath,
        override var id: Long? = null,
        override var whiteboardId: Long? = null
    ) : Update(id, whiteboardId), HasPath

    data class AddPicture(
        override val picture: AddedPicture,
        override var id: Long? = null,
        override var whiteboardId: Long? = null
    ) : Update(id, whiteboardId), HasPicture

    data class RemovePicture(
        override val picture: AddedPicture,
        override var id: Long? = null,
        override var whiteboardId: Long? = null
    ) : Update(id, whiteboardId), HasPicture

    interface HasPath {
        val path: DrawnPath
    }

    interface HasPicture {
        val picture: AddedPicture
    }

    fun undo(): Update {
        return when (this) {
            is AddPath -> RemovePath(path, null, whiteboardId)
            is RemovePath -> AddPath(path, null, whiteboardId)
            is Erase -> RemoveErase(path, null, whiteboardId)
            is RemoveErase -> Erase(path, null, whiteboardId)

            is AddPicture -> RemovePicture(picture, null, whiteboardId)
            is RemovePicture -> AddPicture(picture, null, whiteboardId)
        }
    }

    fun copyWithPath(newPath: DrawnPath): Update {
        return when (this) {
            is AddPath -> this.copy(path = newPath)
            is RemovePath -> this.copy(path = newPath)
            is Erase -> this.copy(path = newPath)
            is RemoveErase -> this.copy(path = newPath)
            else -> this
        }
    }
}