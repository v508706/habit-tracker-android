package com.habittracker.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.habittracker.app.data.local.entities.CompletionEntity
import com.habittracker.app.data.local.entities.HabitEntity
import com.habittracker.app.data.local.entities.UserProfileEntity

@Database(
    entities = [HabitEntity::class, CompletionEntity::class, UserProfileEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun completionDao(): CompletionDao
    abstract fun userProfileDao(): UserProfileDao
}
