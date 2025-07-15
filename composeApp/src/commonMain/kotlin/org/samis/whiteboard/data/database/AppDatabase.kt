package org.samis.whiteboard.data.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.samis.whiteboard.data.database.converter.InstantConverter
import org.samis.whiteboard.data.database.converter.ListConverter
import org.samis.whiteboard.data.database.converter.PathConverter
import org.samis.whiteboard.data.database.dao.PathDao
import org.samis.whiteboard.data.database.dao.UpdateDao
import org.samis.whiteboard.data.database.dao.WhiteboardDao
import org.samis.whiteboard.data.database.entity.PathEntity
import org.samis.whiteboard.data.database.entity.UpdateEntity
import org.samis.whiteboard.data.database.entity.WhiteboardEntity

@Database(
    version = 12,
    entities = [PathEntity::class, WhiteboardEntity::class, UpdateEntity::class],
    exportSchema = true
)
@ConstructedBy(AppDatabaseConstructor::class)
@TypeConverters(PathConverter::class, InstantConverter::class, ListConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pathDao(): PathDao
    abstract fun whiteboardDao(): WhiteboardDao
    abstract fun updateDao(): UpdateDao
}