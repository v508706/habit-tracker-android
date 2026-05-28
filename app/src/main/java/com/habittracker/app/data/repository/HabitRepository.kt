package com.habittracker.app.data.repository

import com.habittracker.app.domain.model.Habit
import com.habittracker.app.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface HabitRepository {
    fun getHabitsFlow(): Flow<List<Habit>>
    suspend fun getHabits(): List<Habit>
    suspend fun saveHabit(habit: Habit)
    suspend fun deleteHabit(habitId: String)
    suspend fun toggleCompletion(habitId: String, date: String): Boolean
    suspend fun getCompletionsForDate(date: String): Map<String, Boolean>
    fun getCompletionsForDateFlow(date: String): Flow<Map<String, Boolean>>
    suspend fun getAllCompletionsForHabit(habitId: String): Map<String, Boolean>
    suspend fun getCompletionsBetween(startDate: String, endDate: String): Map<String, Map<String, Boolean>>
    suspend fun getTotalCompletionCount(): Int
    suspend fun getProfile(): UserProfile?
    suspend fun saveProfile(uid: String, profile: UserProfile)
    suspend fun syncFromFirestore(uid: String)
    suspend fun clearLocalData()
}
