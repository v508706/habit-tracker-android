package com.habittracker.app.domain.model

data class HabitCompletion(
    val habitId: String,
    val date: String,
    val done: Boolean
)
