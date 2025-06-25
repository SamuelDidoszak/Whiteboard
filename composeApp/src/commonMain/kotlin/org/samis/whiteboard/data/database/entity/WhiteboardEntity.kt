package org.samis.whiteboard.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant
import org.samis.whiteboard.data.util.Constant.WHITEBOARD_TABLE_NAME

@Entity(tableName = WHITEBOARD_TABLE_NAME)
data class WhiteboardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    val name: String,
    val createTime: Instant,
    val lastEdited: Instant,
    val canvasColor: Int,
    val pointer: Int?,
    val miniatureSrc: String?
)
