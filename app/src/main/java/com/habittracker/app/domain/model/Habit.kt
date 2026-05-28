package com.habittracker.app.domain.model

data class Habit(
    val id: String,
    val name: String,
    val emoji: String,
    val color: String,
    val days: List<String>,
    val time: String,
    val createdAt: Long = System.currentTimeMillis()
)
