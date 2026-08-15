package org.samis.whiteboard.data.database

import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import kotlinx.coroutines.Dispatchers
import org.samis.whiteboard.data.util.Constant.PALETTE_TABLE_NAME
import org.samis.whiteboard.data.util.Constant.PATH_TABLE_NAME
import org.samis.whiteboard.data.util.Constant.WHITEBOARD_TABLE_NAME

fun getRoomDatabase(
    builder: RoomDatabase.Builder<AppDatabase>,
): AppDatabase {
    return builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .addMigrations(MIGRATION_13_14, MIGRATION_14_15, MIGRATION_15_16, MIGRATION_16_17)
        .build()
}

private val MIGRATION_16_17 = object : Migration(16, 17) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE $PATH_TABLE_NAME ADD COLUMN points TEXT NOT NULL DEFAULT ''")

        val statement = connection.prepare("SELECT id, path FROM $PATH_TABLE_NAME")
        try {
            while (statement.step()) {
                val id = statement.getLong(0)
                val pathString = statement.getText(1)
                val points = convertOldPathToPoints(pathString)
                val escapedPoints = points.replace("'", "''")
                connection.execSQL("UPDATE $PATH_TABLE_NAME SET points = '$escapedPoints' WHERE id = $id")
            }
        } finally {
            statement.close()
        }

        connection.execSQL("""
            CREATE TABLE ${PATH_TABLE_NAME}_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                drawingTool TEXT NOT NULL,
                points TEXT NOT NULL,
                strokeWidth REAL NOT NULL,
                strokeColor INTEGER NOT NULL,
                fillColor INTEGER NOT NULL,
                opacity REAL NOT NULL
            )
        """)
        connection.execSQL("""
            INSERT INTO ${PATH_TABLE_NAME}_new
            SELECT id, drawingTool, points, strokeWidth, strokeColor, fillColor, opacity
            FROM $PATH_TABLE_NAME
        """)
        connection.execSQL("DROP TABLE $PATH_TABLE_NAME")
        connection.execSQL("ALTER TABLE ${PATH_TABLE_NAME}_new RENAME TO $PATH_TABLE_NAME")
    }
}

private fun convertOldPathToPoints(pathString: String): String {
    if (pathString.isBlank()) return ""
    return pathString.trim().split(" ")
        .filter { it.isNotEmpty() }
        .map { it.substring(1) }
        .joinToString(";")
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