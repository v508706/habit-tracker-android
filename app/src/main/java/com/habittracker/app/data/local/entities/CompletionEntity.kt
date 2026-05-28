package com.habittracker.app.data.local.entities

import androidx.room.Entity

@Entity(tableName = "completions", primaryKeys = ["habitId", "date"])
data class CompletionEntity(
    val habitId: String,
    val date: String,
    val done: Boolean
)
