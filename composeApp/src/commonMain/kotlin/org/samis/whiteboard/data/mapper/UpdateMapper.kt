package org.samis.whiteboard.data.mapper

import org.samis.whiteboard.data.database.entity.PathEntity
import org.samis.whiteboard.data.database.entity.UpdateEntity
import org.samis.whiteboard.domain.model.Update
import org.samis.whiteboard.domain.model.Update.AddPath
import org.samis.whiteboard.domain.model.Update.Erase
import org.samis.whiteboard.domain.model.Update.RemoveErase
import org.samis.whiteboard.domain.model.Update.RemovePath

fun UpdateEntity.toUpdate(path: PathEntity): Update {
    val path = path.toDrawnPath()
    return when (updateType.uppercase()) {
        "AddPath".uppercase() -> AddPath(path, id, whiteboardId)
        "RemovePath".uppercase() -> RemovePath(path, id, whiteboardId)
        "Erase".uppercase() -> Erase(path, id, whiteboardId)
        "RemoveErase".uppercase() -> RemoveErase(path, id, whiteboardId)
        else -> {
            throw IllegalArgumentException("Unknown UpdateType: $updateType")
        }
    }
}

fun Update.toUpdateEntity(): UpdateEntity {
    return when (this) {
        is AddPath -> UpdateEntity(id, "AddPath", path.id, whiteboardId!!)
        is RemovePath -> UpdateEntity(id, "RemovePath", path.id, whiteboardId!!)
        is Erase -> UpdateEntity(id, "Erase", path.id, whiteboardId!!)
        is RemoveErase -> UpdateEntity(id, "RemoveErase", path.id, whiteboardId!!)
    }
}