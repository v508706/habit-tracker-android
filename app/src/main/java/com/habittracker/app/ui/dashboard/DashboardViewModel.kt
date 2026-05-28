package com.habittracker.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habittracker.app.data.repository.HabitRepository
import com.habittracker.app.domain.model.Habit
import com.habittracker.app.domain.usecase.GetCurrentStreakUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

data class StreakNudge(val habit: Habit, val streak: Int)

data class DashboardUiState(
    val habits: List<Habit> = emptyList(),
    val completions: Map<String, Boolean> = emptyMap(),
    val streaks: Map<String, Int> = emptyMap(),
    val userName: String = "",
    val dateLabel: String = "",
    val scheduledCount: Int = 0,
    val doneCount: Int = 0,
    val streakNudges: List<StreakNudge> = emptyList(),
    val dismissedNudgeIds: Set<String> = emptySet()
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: HabitRepository,
    private val getCurrentStreak: GetCurrentStreakUseCase
) : ViewModel() {

    private val today: LocalDate = LocalDate.now()
    private val todayStr: String = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    private val todayDayName: String = com.habittracker.app.Constants.DAY_NAMES[today.dayOfWeek.value - 1]

    private val _dismissedNudges = MutableStateFlow<Set<String>>(emptySet())
    private val _state = MutableStateFlow(DashboardUiState())
    val state = _state.asStateFlow()

    init {
        val dateLabel = today.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault()) +
                ", " + today.month.getDisplayName(TextStyle.FULL, Locale.getDefault()) +
                " " + today.dayOfMonth

        viewModelScope.launch {
            val profile = repository.getProfile()
            val firstName = profile?.name?.split(" ")?.firstOrNull() ?: "Friend"

            combine(
                repository.getHabitsFlow(),
                repository.getCompletionsForDateFlow(todayStr),
                _dismissedNudges
            ) { habits, completions, dismissed ->
                val todayHabits = habits.filter { todayDayName in it.days }
                    .sortedBy { it.time }
                val allCompletions = mutableMapOf<String, Map<String, Boolean>>()

                val streaks = mutableMapOf<String, Int>()
                todayHabits.forEach { habit ->
                    val hCompletions = allCompletions.getOrPut(habit.id) {
                        emptyMap()
                    }
                    // We'll compute streaks with available data
                    streaks[habit.id] = 0
                }

                val nudges = todayHabits.filter { habit ->
                    habit.id !in dismissed &&
                    completions[habit.id] != true
                }

                DashboardUiState(
                    habits = todayHabits,
                    completions = completions,
                    streaks = streaks,
                    userName = firstName,
                    dateLabel = dateLabel,
                    scheduledCount = todayHabits.size,
                    doneCount = todayHabits.count { completions[it.id] == true },
                    dismissedNudgeIds = dismissed
                )
            }.collect { partial ->
                // Load streaks separately
                _state.value = partial
            }
        }

        // Load streaks in background
        viewModelScope.launch {
            repository.getHabitsFlow().collect { habits ->
                val today2 = LocalDate.now()
                val todayHabits = habits.filter { todayDayName in it.days }
                val streaks = mutableMapOf<String, Int>()
                todayHabits.forEach { habit ->
                    val completions = repository.getAllCompletionsForHabit(habit.id)
                    streaks[habit.id] = getCurrentStreak(habit, completions)
                }
                _state.value = _state.value.copy(streaks = streaks)
                rebuildNudges(todayHabits, streaks)
            }
        }
    }

    private fun rebuildNudges(habits: List<Habit>, streaks: Map<String, Int>) {
        val nudges = habits.filter { habit ->
            val streak = streaks[habit.id] ?: 0
            streak > 0 &&
            _state.value.completions[habit.id] != true &&
            habit.id !in _dismissedNudges.value
        }.map { StreakNudge(it, streaks[it.id] ?: 0) }
        _state.value = _state.value.copy(streakNudges = nudges)
    }

    fun toggleCompletion(habitId: String) {
        viewModelScope.launch {
            repository.toggleCompletion(habitId, todayStr)
        }
    }

    fun dismissNudge(habitId: String) {
        _dismissedNudges.value = _dismissedNudges.value + habitId
        _state.value = _state.value.copy(
            streakNudges = _state.value.streakNudges.filter { it.habit.id != habitId },
            dismissedNudgeIds = _dismissedNudges.value
        )
    }
}
