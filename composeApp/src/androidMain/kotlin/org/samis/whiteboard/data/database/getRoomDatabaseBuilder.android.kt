package org.samis.whiteboard.data.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import org.samis.whiteboard.data.util.Constant.APP_DATABASE_NAME

fun getRoomDatabaseBuilder(context: Context): RoomDatabase.Builder<AppDatabase> {
    val dbFile = context.getDatabasePath(APP_DATABASE_NAME)
    return Room.databaseBuilder<AppDatabase>(
        context = context,
        name = dbFile.absolutePath
    )
}