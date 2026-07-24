package org.samis.whiteboard.data.database

import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import kotlinx.coroutines.Dispatchers
import org.samis.whiteboard.data.util.Constant.PALETTE_TABLE_NAME
import org.samis.whiteboard.data.util.Constant.WHITEBOARD_TABLE_NAME

fun getRoomDatabase(
    builder: RoomDatabase.Builder<AppDatabase>,
): AppDatabase {
    return builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .addMigrations(MIGRATION_13_14, MIGRATION_14_15, MIGRATION_15_16)
        .build()
}

private val MIGRATION_15_16 = object : Migration(15, 16) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE $WHITEBOARD_TABLE_NAME ADD COLUMN canvasScale FLOAT NOT NULL DEFAULT 1")
    }
}

private val MIGRATION_14_15 = object : Migration(14, 15) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE $WHITEBOARD_TABLE_NAME ADD COLUMN canvasOffsetX FLOAT NOT NULL DEFAULT 0")
        connection.execSQL("ALTER TABLE $WHITEBOARD_TABLE_NAME ADD COLUMN canvasOffsetY FLOAT NOT NULL DEFAULT 0")
    }
}

private val MIGRATION_13_14 = object : Migration(13, 14) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE $WHITEBOARD_TABLE_NAME ADD COLUMN canvasWidth INTEGER NOT NULL DEFAULT 0")
        connection.execSQL("ALTER TABLE $WHITEBOARD_TABLE_NAME ADD COLUMN canvasHeight INTEGER NOT NULL DEFAULT 0")
    }
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