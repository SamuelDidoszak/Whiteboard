package org.samis.whiteboard.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import org.samis.whiteboard.data.database.entity.PathEntity
import org.samis.whiteboard.data.util.Constant.PATH_TABLE_NAME

@Dao
interface PathDao {
    @Upsert
    suspend fun upsertPath(pathEntity: PathEntity)

    @Delete
    suspend fun deletePath(pathEntity: PathEntity)

    @Query("SELECT * FROM $PATH_TABLE_NAME WHERE whiteboardId = :whiteboardId")
    fun getPathsForWhiteboard(whiteboardId: Long): Flow<List<PathEntity>>

    @Query("SELECT * FROM $PATH_TABLE_NAME WHERE id = :pathId")
    suspend fun getPathById(pathId: Long): PathEntity?
}