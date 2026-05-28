package com.habittracker.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.habittracker.app.data.local.CompletionDao
import com.habittracker.app.data.local.HabitDao
import com.habittracker.app.data.local.UserProfileDao
import com.habittracker.app.data.local.entities.CompletionEntity
import com.habittracker.app.data.local.entities.UserProfileEntity
import com.habittracker.app.data.local.toDomain
import com.habittracker.app.data.local.toEntity
import com.habittracker.app.data.remote.FirestoreDataSource
import com.habittracker.app.domain.model.Habit
import com.habittracker.app.domain.model.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.TimeZone
import javax.inject.Inject

class HabitRepositoryImpl @Inject constructor(
    private val habitDao: HabitDao,
    private val completionDao: CompletionDao,
    private val userProfileDao: UserProfileDao,
    private val firestoreDataSource: FirestoreDataSource,
    private val auth: FirebaseAuth
) : HabitRepository {

    private val bgScope = CoroutineScope(Dispatchers.IO)

    override fun getHabitsFlow(): Flow<List<Habit>> =
        habitDao.getHabitsFlow().map { list -> list.map { it.toDomain() } }

    override suspend fun getHabits(): List<Habit> =
        habitDao.getHabits().map { it.toDomain() }

    override suspend fun saveHabit(habit: Habit) {
        habitDao.insertHabit(habit.toEntity())
        bgScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            runCatching { firestoreDataSource.saveHabit(uid, habit) }
        }
    }

    override suspend fun deleteHabit(habitId: String) {
        habitDao.deleteHabitById(habitId)
        bgScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            runCatching { firestoreDataSource.deleteHabit(uid, habitId) }
        }
    }

    override suspend fun toggleCompletion(habitId: String, date: String): Boolean {
        val existing = completionDao.getCompletionsForDate(date)
            .firstOrNull { it.habitId == habitId }
        val newDone = !(existing?.done ?: false)
        completionDao.insertCompletion(CompletionEntity(habitId, date, newDone))
        bgScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            runCatching { firestoreDataSource.setCompletion(uid, habitId, date, newDone) }
        }
        return newDone
    }

    override suspend fun getCompletionsForDate(date: String): Map<String, Boolean> =
        completionDao.getCompletionsForDate(date).associate { it.habitId to it.done }

    override fun getCompletionsForDateFlow(date: String): Flow<Map<String, Boolean>> =
        completionDao.getCompletionsForDateFlow(date).map { list ->
            list.associate { it.habitId to it.done }
        }

    override suspend fun getAllCompletionsForHabit(habitId: String): Map<String, Boolean> =
        completionDao.getCompletionsForHabit(habitId).associate { it.date to it.done }

    override suspend fun getCompletionsBetween(
        startDate: String,
        endDate: String
    ): Map<String, Map<String, Boolean>> {
        val list = completionDao.getCompletionsBetween(startDate, endDate)
        val result = mutableMapOf<String, MutableMap<String, Boolean>>()
        list.forEach { entity ->
            result.getOrPut(entity.date) { mutableMapOf() }[entity.habitId] = entity.done
        }
        return result
    }

    override suspend fun getTotalCompletionCount(): Int =
        completionDao.getTotalCompletionCount()

    override suspend fun getProfile(): UserProfile? {
        val entity = userProfileDao.getProfile() ?: return null
        return UserProfile(
            name = entity.name,
            setupDone = entity.setupDone,
            timezone = entity.timezone
        )
    }

    override suspend fun saveProfile(uid: String, profile: UserProfile) {
        userProfileDao.insertProfile(
            UserProfileEntity(uid, profile.name, profile.setupDone, profile.timezone)
        )
        bgScope.launch {
            runCatching { firestoreDataSource.saveUserProfile(uid, profile) }
        }
    }

    override suspend fun syncFromFirestore(uid: String) {
        // Firestore wins on login sync
        val remoteProfile = runCatching { firestoreDataSource.getUserProfile(uid) }.getOrNull()
        remoteProfile?.let {
            userProfileDao.insertProfile(UserProfileEntity(uid, it.name, it.setupDone, it.timezone))
        }

        val remoteHabits = runCatching { firestoreDataSource.getHabits(uid) }.getOrElse { emptyList() }
        if (remoteHabits.isNotEmpty()) {
            habitDao.deleteAllHabits()
            habitDao.insertHabits(remoteHabits.map { it.toEntity() })
        }

        val remoteCompletions = runCatching { firestoreDataSource.getCompletions(uid) }.getOrElse { emptyMap() }
        val completionEntities = remoteCompletions.flatMap { (date, habitMap) ->
            habitMap.map { (habitId, done) -> CompletionEntity(habitId, date, done) }
        }
        if (completionEntities.isNotEmpty()) {
            completionDao.deleteAllCompletions()
            completionDao.insertCompletions(completionEntities)
        }
    }

    override suspend fun clearLocalData() {
        habitDao.deleteAllHabits()
        completionDao.deleteAllCompletions()
        userProfileDao.deleteAllProfiles()
    }
}
