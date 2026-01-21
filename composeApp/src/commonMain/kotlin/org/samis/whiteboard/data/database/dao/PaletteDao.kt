package org.samis.whiteboard.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import org.samis.whiteboard.data.database.entity.PaletteEntity
import org.samis.whiteboard.data.util.Constant.PALETTE_TABLE_NAME

@Dao
interface PaletteDao {
    @Upsert
    suspend fun upsertPalette(paletteEntity: PaletteEntity): Long

    suspend fun deletePalette(paletteEntity: PaletteEntity) =
        deletePaletteByContent(
            paletteEntity.background,
            paletteEntity.foreground,
            paletteEntity.red,
            paletteEntity.blue,
            paletteEntity.green,
            paletteEntity.others
        )

    @Query("""
    DELETE FROM $PALETTE_TABLE_NAME 
    WHERE rowid = (
        SELECT rowid FROM $PALETTE_TABLE_NAME
        WHERE background = :background 
        AND foreground = :foreground 
        AND red = :red 
        AND blue = :blue 
        AND green = :green
        AND `others` = :others
        ORDER BY rowid DESC
        LIMIT 1
    )
""")
    suspend fun deletePaletteByContent(
        background: Int,
        foreground: Int,
        red: Int,
        blue: Int,
        green: Int,
        others: List<Int>
    )

    @Query("SELECT * FROM $PALETTE_TABLE_NAME ORDER BY id DESC")
    fun getAllPalettes(): Flow<List<PaletteEntity>>

    @Query("SELECT * FROM $PALETTE_TABLE_NAME WHERE id = :paletteId")
    suspend fun getPaletteById(paletteId: Long): PaletteEntity?
}