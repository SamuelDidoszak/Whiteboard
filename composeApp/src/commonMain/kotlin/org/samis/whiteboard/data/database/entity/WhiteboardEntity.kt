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
    val lastModified: Instant,
    val palette: List<Int>,
    val markerColors: List<Int>,
    val strokeWidths: List<Float>,
    val activeStrokeWidthButton: Int,
    val opacity: Float,
    val fillColor: Int,
    val pointer: Int?,
    val miniatureSrc: String?
)
