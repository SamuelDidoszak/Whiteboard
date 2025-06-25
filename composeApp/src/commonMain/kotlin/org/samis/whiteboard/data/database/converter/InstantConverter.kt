package org.samis.whiteboard.data.database.converter

import androidx.room.TypeConverter
import kotlinx.datetime.Instant

class InstantConverter {

    @TypeConverter
    fun fromInstant(date: Instant): String {
        return date.toString()
    }

    @TypeConverter
    fun toInstant(date: String): Instant {
        return Instant.parse(input = date)
    }
}