package org.samis.whiteboard.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.samis.whiteboard.data.util.Constant.PALETTE_TABLE_NAME

@Entity(tableName = PALETTE_TABLE_NAME)
data class PaletteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    val background: Int,
    val foreground: Int,
    val red: Int,
    val blue: Int,
    val green: Int,
    val others: List<Int> = listOf()
)