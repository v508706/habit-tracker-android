package com.habittracker.app.data.local

import com.habittracker.app.data.local.entities.CompletionEntity
import com.habittracker.app.data.local.entities.HabitEntity
import com.habittracker.app.domain.model.Habit
import com.habittracker.app.domain.model.HabitCompletion
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun HabitEntity.toDomain(): Habit = Habit(
    id = id,
    name = name,
    emoji = emoji,
    color = color,
    days = Json.decodeFromString(days),
    time = time,
    createdAt = createdAt
)

fun Habit.toEntity(): HabitEntity = HabitEntity(
    id = id,
    name = name,
    emoji = emoji,
    color = color,
    days = Json.encodeToString(days),
    time = time,
    createdAt = createdAt
)

fun CompletionEntity.toDomain() = HabitCompletion(habitId, date, done)

fun HabitCompletion.toEntity() = CompletionEntity(habitId, date, done)
