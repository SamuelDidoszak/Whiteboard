package org.samis.whiteboard.domain.model

sealed class Update(
    open val id: Long? = null,
    open val whiteboardId: Long? = null
) {
    data class AddPath(
        override val path: DrawnPath,
        override val id: Long? = null,
        override val whiteboardId: Long? = null
    ) : Update(id, whiteboardId), HasPath

    data class RemovePath(
        override val path: DrawnPath,
        override val id: Long? = null,
        override val whiteboardId: Long? = null
    ) : Update(id, whiteboardId), HasPath

    data class Erase(
        override val path: DrawnPath,
        override val id: Long? = null,
        override val whiteboardId: Long? = null
    ) : Update(id, whiteboardId), HasPath

    data class RemoveErase(
        override val path: DrawnPath,
        override val id: Long? = null,
        override val whiteboardId: Long? = null
    ) : Update(id, whiteboardId), HasPath

    interface HasPath {
        val path: DrawnPath
    }

    fun undo(): Update {
        return when (this) {
            is AddPath -> RemovePath(path, id, whiteboardId)
            is RemovePath -> AddPath(path, id, whiteboardId)
            is Erase -> RemoveErase(path, id, whiteboardId)
            is RemoveErase -> Erase(path, id, whiteboardId)
        }
    }

    fun copyWithPath(newPath: DrawnPath): Update {
        return when (this) {
            is Update.AddPath -> this.copy(path = newPath)
            is Update.RemovePath -> this.copy(path = newPath)
            is Update.Erase -> this.copy(path = newPath)
            is Update.RemoveErase -> this.copy(path = newPath)
        }
    }
}