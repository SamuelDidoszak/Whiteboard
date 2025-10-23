package org.samis.whiteboard.data.database

import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import kotlinx.coroutines.Dispatchers
import org.samis.whiteboard.data.util.Constant.PALETTE_TABLE_NAME

fun getRoomDatabase(
    builder: RoomDatabase.Builder<AppDatabase>,
): AppDatabase {
    return builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .addMigrations(MIGRATION_12_13)
        .build()
}


private val MIGRATION_12_13 = object : Migration(12, 13) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `$PALETTE_TABLE_NAME` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT,
                `background` INTEGER NOT NULL,
                `foreground` INTEGER NOT NULL,
                `red` INTEGER NOT NULL,
                `blue` INTEGER NOT NULL,
                `green` INTEGER NOT NULL,
                `others` TEXT NOT NULL
            )
            """.trimIndent()
        )
    }
}