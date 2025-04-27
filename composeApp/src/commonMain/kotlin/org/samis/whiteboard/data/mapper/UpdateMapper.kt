package org.samis.whiteboard.data.mapper

import org.samis.whiteboard.data.database.entity.PathEntity
import org.samis.whiteboard.data.database.entity.UpdateEntity
import org.samis.whiteboard.domain.model.UpdateType
import org.samis.whiteboard.domain.model.UpdateType.AddPath
import org.samis.whiteboard.domain.model.UpdateType.Erase
import org.samis.whiteboard.domain.model.UpdateType.RemoveErase
import org.samis.whiteboard.domain.model.UpdateType.RemovePath

fun UpdateEntity.toUpdate(path: PathEntity): UpdateType {
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

fun UpdateType.toUpdateEntity(): UpdateEntity {
    return when (this) {
        is AddPath -> UpdateEntity(id, "AddPath", path.id, whiteboardId!!)
        is RemovePath -> UpdateEntity(id, "RemovePath", path.id, whiteboardId!!)
        is Erase -> UpdateEntity(id, "Erase", path.id, whiteboardId!!)
        is RemoveErase -> UpdateEntity(id, "RemoveErase", path.id, whiteboardId!!)
    }
}