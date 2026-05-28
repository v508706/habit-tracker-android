package com.habittracker.app.data.local

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    @TypeConverter
    fun fromDaysList(days: List<String>): String = Json.encodeToString(days)

    @TypeConverter
    fun toDaysList(daysJson: String): List<String> = Json.decodeFromString(daysJson)
}
