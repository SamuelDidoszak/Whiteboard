package org.samis.whiteboard.data.database.entity

import androidx.compose.ui.graphics.Path
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.samis.whiteboard.data.util.Constant.PATH_TABLE_NAME
import org.samis.whiteboard.domain.model.DrawingTool

@Entity(tableName = PATH_TABLE_NAME)
data class PathEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    val drawingTool: DrawingTool,
    val path: Path,
    val strokeWidth: Float,
    val strokeColor: Int,
    val fillColor: Int,
    val opacity: Float
)
