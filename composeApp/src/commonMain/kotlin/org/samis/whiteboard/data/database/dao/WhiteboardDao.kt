package org.samis.whiteboard.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.samis.whiteboard.data.database.entity.WhiteboardEntity
import org.samis.whiteboard.data.util.Constant.WHITEBOARD_TABLE_NAME

@Dao
interface WhiteboardDao {

    @Insert
    suspend fun insertWhiteboard(whiteboard: WhiteboardEntity): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateWhiteboard(whiteboard: WhiteboardEntity)

    @Delete
    suspend fun deleteWhiteboard(whiteboard: WhiteboardEntity)

    @Query("SELECT * FROM $WHITEBOARD_TABLE_NAME ORDER BY lastEdited DESC")
    fun getAllWhiteboards(): Flow<List<WhiteboardEntity>>

    @Query("SELECT * FROM $WHITEBOARD_TABLE_NAME WHERE id = :id")
    suspend fun getWhiteboardById(id: Long): WhiteboardEntity?
}