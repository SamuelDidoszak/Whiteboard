package org.samis.whiteboard.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import org.samis.whiteboard.data.database.entity.PaletteEntity
import org.samis.whiteboard.data.util.Constant.PALETTE_TABLE_NAME

@Dao
interface PaletteDao {
    @Upsert
    suspend fun upsertPalette(paletteEntity: PaletteEntity): Long

    @Delete
    suspend fun deletePalette(paletteEntity: PaletteEntity)

    @Query("SELECT * FROM $PALETTE_TABLE_NAME WHERE id = :paletteId")
    suspend fun getPaletteById(paletteId: Long): PaletteEntity?
}