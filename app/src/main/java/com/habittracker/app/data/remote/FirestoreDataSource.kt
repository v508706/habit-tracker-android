package com.habittracker.app.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.habittracker.app.domain.model.Habit
import com.habittracker.app.domain.model.HabitCompletion
import com.habittracker.app.domain.model.UserProfile
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirestoreDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun saveUserProfile(uid: String, profile: UserProfile) {
        firestore.collection("users").document(uid)
            .collection("profile").document("data")
            .set(mapOf(
                "name" to profile.name,
                "setupDone" to profile.setupDone,
                "timezone" to profile.timezone,
                "fcmToken" to profile.fcmToken
            ), SetOptions.merge())
            .await()
    }

    suspend fun getUserProfile(uid: String): UserProfile? {
        val doc = firestore.collection("users").document(uid)
            .collection("profile").document("data")
            .get().await()
        if (!doc.exists()) return null
        return UserProfile(
            name = doc.getString("name") ?: "",
            setupDone = doc.getBoolean("setupDone") ?: false,
            timezone = doc.getString("timezone") ?: "UTC",
            fcmToken = doc.getString("fcmToken")
        )
    }

    suspend fun saveHabit(uid: String, habit: Habit) {
        firestore.collection("users").document(uid)
            .collection("habits").document(habit.id)
            .set(mapOf(
                "id" to habit.id,
                "name" to habit.name,
                "emoji" to habit.emoji,
                "color" to habit.color,
                "days" to habit.days,
                "time" to habit.time,
                "createdAt" to habit.createdAt
            )).await()
    }

    suspend fun deleteHabit(uid: String, habitId: String) {
        firestore.collection("users").document(uid)
            .collection("habits").document(habitId)
            .delete().await()
    }

    suspend fun getHabits(uid: String): List<Habit> {
        val snapshot = firestore.collection("users").document(uid)
            .collection("habits").get().await()
        return snapshot.documents.mapNotNull { doc ->
            try {
                @Suppress("UNCHECKED_CAST")
                Habit(
                    id = doc.getString("id") ?: doc.id,
                    name = doc.getString("name") ?: return@mapNotNull null,
                    emoji = doc.getString("emoji") ?: "📝",
                    color = doc.getString("color") ?: "#6366f1",
                    days = (doc.get("days") as? List<String>) ?: emptyList(),
                    time = doc.getString("time") ?: "08:00",
                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                )
            } catch (e: Exception) { null }
        }
    }

    suspend fun setCompletion(uid: String, habitId: String, date: String, done: Boolean) {
        firestore.collection("users").document(uid)
            .collection("completions").document(date)
            .set(mapOf(habitId to done), SetOptions.merge())
            .await()
    }

    suspend fun getCompletions(uid: String): Map<String, Map<String, Boolean>> {
        val snapshot = firestore.collection("users").document(uid)
            .collection("completions").get().await()
        return snapshot.documents.associate { doc ->
            @Suppress("UNCHECKED_CAST")
            doc.id to (doc.data as? Map<String, Boolean> ?: emptyMap())
        }
    }

    suspend fun saveFcmToken(uid: String, token: String) {
        firestore.collection("users").document(uid)
            .collection("profile").document("data")
            .set(mapOf("fcmToken" to token), SetOptions.merge())
            .await()
    }
}
