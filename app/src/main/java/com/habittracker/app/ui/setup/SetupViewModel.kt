package com.habittracker.app.ui.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.habittracker.app.Constants
import com.habittracker.app.data.repository.HabitRepository
import com.habittracker.app.data.remote.FirestoreDataSource
import com.habittracker.app.domain.model.Habit
import com.habittracker.app.domain.model.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.TimeZone
import java.util.UUID
import javax.inject.Inject

data class HabitDraft(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val emoji: String = "🎯",
    val color: String = "#6366f1",
    val days: List<String> = listOf("Mon", "Tue", "Wed", "Thu", "Fri"),
    val time: String = "08:00"
)

data class SetupUiState(
    val step: Int = 0,
    val userName: String = "",
    val habits: List<HabitDraft> = emptyList(),
    val draft: HabitDraft = HabitDraft(),
    val showForm: Boolean = false,
    val saving: Boolean = false,
    val done: Boolean = false
)

@HiltViewModel
class SetupViewModel @Inject constructor(
    private val repository: HabitRepository,
    private val firestoreDataSource: FirestoreDataSource,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _state = MutableStateFlow(SetupUiState())
    val state = _state.asStateFlow()

    fun setUserName(name: String) {
        _state.value = _state.value.copy(userName = name.take(40))
    }

    fun nextStep() {
        _state.value = _state.value.copy(step = _state.value.step + 1)
    }

    fun updateDraft(draft: HabitDraft) {
        _state.value = _state.value.copy(draft = draft)
    }

    fun showForm() {
        _state.value = _state.value.copy(showForm = true, draft = HabitDraft())
    }

    fun hideForm() {
        _state.value = _state.value.copy(showForm = false)
    }

    fun addHabit() {
        val draft = _state.value.draft
        if (draft.name.isBlank() || draft.days.isEmpty()) return
        _state.value = _state.value.copy(
            habits = _state.value.habits + draft,
            showForm = false,
            draft = HabitDraft()
        )
    }

    fun removeHabit(id: String) {
        _state.value = _state.value.copy(
            habits = _state.value.habits.filter { it.id != id }
        )
    }

    fun completeSetup() {
        viewModelScope.launch {
            _state.value = _state.value.copy(saving = true)
            try {
                val uid = auth.currentUser?.uid ?: return@launch
                val timezone = TimeZone.getDefault().id

                val fcmToken = runCatching {
                    FirebaseMessaging.getInstance().token.await()
                }.getOrNull()

                val profile = UserProfile(
                    name = _state.value.userName.ifBlank { "Friend" },
                    setupDone = true,
                    timezone = timezone,
                    fcmToken = fcmToken
                )
                repository.saveProfile(uid, profile)

                _state.value.habits.forEach { draft ->
                    repository.saveHabit(
                        Habit(draft.id, draft.name, draft.emoji, draft.color, draft.days, draft.time)
                    )
                }

                _state.value = _state.value.copy(saving = false, done = true)
            } catch (e: Exception) {
                _state.value = _state.value.copy(saving = false)
            }
        }
    }
}
