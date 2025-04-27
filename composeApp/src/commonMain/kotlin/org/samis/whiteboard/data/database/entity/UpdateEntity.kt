package org.samis.whiteboard.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.samis.whiteboard.data.util.Constant.UPDATE_TABLE_NAME

@Entity(tableName = UPDATE_TABLE_NAME)
data class UpdateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    val updateType: String,
    val pathId: Long?,
    val whiteboardId: Long
)