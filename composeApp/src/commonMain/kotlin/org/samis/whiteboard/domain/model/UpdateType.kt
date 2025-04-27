package org.samis.whiteboard.domain.model

sealed class UpdateType(
    val id: Long? = null,
    val whiteboardId: Long? = null) {

    class AddPath(val path: DrawnPath, id: Long?, whiteboardId: Long?): UpdateType(id, whiteboardId)
    class RemovePath(val path: DrawnPath, id: Long?, whiteboardId: Long?): UpdateType(id, whiteboardId)
    class Erase(val path: DrawnPath, id: Long?, whiteboardId: Long?): UpdateType(id, whiteboardId)
    class RemoveErase(val path: DrawnPath, id: Long?, whiteboardId: Long?): UpdateType(id, whiteboardId)

    fun undo(update: UpdateType): UpdateType {
        return when(update) {
            is AddPath -> RemovePath(update.path, update.id, update.whiteboardId)
            is RemovePath -> AddPath(update.path, update.id, update.whiteboardId)
            is Erase -> RemoveErase(update.path, update.id, update.whiteboardId)
            is RemoveErase -> Erase(update.path, update.id, update.whiteboardId)
        }
    }
}