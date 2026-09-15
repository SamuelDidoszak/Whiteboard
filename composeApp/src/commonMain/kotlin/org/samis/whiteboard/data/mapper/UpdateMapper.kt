package org.samis.whiteboard.data.mapper

import androidx.compose.ui.geometry.Offset
import org.samis.whiteboard.data.database.entity.PathEntity
import org.samis.whiteboard.data.database.entity.UpdateEntity
import org.samis.whiteboard.domain.model.Update
import org.samis.whiteboard.domain.model.Update.AddPath
import org.samis.whiteboard.domain.model.Update.Erase
import org.samis.whiteboard.domain.model.Update.RemoveErase
import org.samis.whiteboard.domain.model.Update.RemovePath
import org.samis.whiteboard.presentation.whiteboard.util.AddedPicture

fun UpdateEntity.toUpdate(path: PathEntity?): Update {
    val path = path?.toDrawnPath()
    return when (updateType.uppercase()) {
        "AddPath".uppercase() -> AddPath(path!!, id, whiteboardId)
        "RemovePath".uppercase() -> RemovePath(path!!, id, whiteboardId)
        "Erase".uppercase() -> Erase(path!!, id, whiteboardId)
        "RemoveErase".uppercase() -> RemoveErase(path!!, id, whiteboardId)
        "AddPicture".uppercase() -> Update.AddPicture(AddedPicture(picturePath = picturePath!!, position = pathId?.toOffset() ?: Offset.Zero), id, whiteboardId)
        "RemovePicture".uppercase() -> Update.RemovePicture(AddedPicture(picturePath = picturePath!!, position = pathId?.toOffset() ?: Offset.Zero), id, whiteboardId)
        else -> {
            throw IllegalArgumentException("Unknown UpdateType: $updateType")
        }
    }
}

fun Update.toUpdateEntity(): UpdateEntity {
    return when (this) {
        is AddPath -> UpdateEntity(id, "AddPath", path.id, null, whiteboardId!!)
        is RemovePath -> UpdateEntity(id, "RemovePath", path.id, null, whiteboardId!!)
        is Erase -> UpdateEntity(id, "Erase", path.id, null, whiteboardId!!)
        is RemoveErase -> UpdateEntity(id, "RemoveErase", path.id, null, whiteboardId!!)
        is Update.AddPicture -> UpdateEntity(id, "AddPicture", picture.position.toLong(), picture.picturePath, whiteboardId!!)
        is Update.RemovePicture -> UpdateEntity(id, "RemovePicture", picture.position.toLong(), picture.picturePath, whiteboardId!!)
    }
}

fun Offset.toLong(): Long {
    val xBits = x.toBits().toLong()
    val yBits = y.toBits().toLong()
    return (xBits shl 32) or (yBits and 0xFFFFFFFFL)
}

fun Long.toOffset(): Offset {
    val x = Float.fromBits((this shr 32).toInt())
    val y = Float.fromBits(this.toInt())
    return Offset(x, y)
}