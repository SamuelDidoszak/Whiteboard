package org.samis.whiteboard.data.database

import androidx.room.Room
import androidx.room.RoomDatabase
import org.samis.whiteboard.data.util.Constant.APP_DATABASE_NAME
import java.io.File

fun getRoomDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val dbFile = File(System.getProperty("java.io.tmpdir"), APP_DATABASE_NAME)
    return Room.databaseBuilder<AppDatabase>(
        name = dbFile.absolutePath
    )
}