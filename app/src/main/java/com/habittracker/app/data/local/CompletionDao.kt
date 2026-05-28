package com.habittracker.app.data.local

import androidx.room.*
import com.habittracker.app.data.local.entities.CompletionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CompletionDao {
    @Query("SELECT * FROM completions WHERE date = :date")
    fun getCompletionsForDateFlow(date: String): Flow<List<CompletionEntity>>

    @Query("SELECT * FROM completions WHERE date = :date")
    suspend fun getCompletionsForDate(date: String): List<CompletionEntity>

    @Query("SELECT * FROM completions WHERE habitId = :habitId ORDER BY date ASC")
    suspend fun getCompletionsForHabit(habitId: String): List<CompletionEntity>

    @Query("SELECT * FROM completions WHERE habitId = :habitId AND date >= :startDate ORDER BY date ASC")
    suspend fun getCompletionsForHabitSince(habitId: String, startDate: String): List<CompletionEntity>

    @Query("SELECT * FROM completions WHERE date >= :startDate AND date <= :endDate ORDER BY date ASC")
    suspend fun getCompletionsBetween(startDate: String, endDate: String): List<CompletionEntity>

    @Query("SELECT COUNT(*) FROM completions WHERE done = 1")
    suspend fun getTotalCompletionCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletion(completion: CompletionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletions(completions: List<CompletionEntity>)

    @Query("DELETE FROM completions WHERE habitId = :habitId")
    suspend fun deleteCompletionsForHabit(habitId: String)

    @Query("DELETE FROM completions")
    suspend fun deleteAllCompletions()
}
