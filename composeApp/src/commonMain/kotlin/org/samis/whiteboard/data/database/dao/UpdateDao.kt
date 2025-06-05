package org.samis.whiteboard.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import org.samis.whiteboard.data.database.entity.UpdateEntity
import org.samis.whiteboard.data.util.Constant.UPDATE_TABLE_NAME

@Dao
interface UpdateDao {

    @Upsert
    suspend fun upsertUpdate(updateEntity: UpdateEntity): Long

    @Delete
    suspend fun deleteUpdate(updateEntity: UpdateEntity)

    @Query("SELECT * FROM $UPDATE_TABLE_NAME WHERE whiteboardId = :whiteboardId")
    fun getWhiteboardUpdates(whiteboardId: Long): Flow<List<UpdateEntity>>
}