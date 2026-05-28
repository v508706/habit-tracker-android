package com.habittracker.app.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habittracker.app.Constants
import com.habittracker.app.data.repository.HabitRepository
import com.habittracker.app.domain.model.Habit
import com.habittracker.app.domain.usecase.GetBestStreakUseCase
import com.habittracker.app.domain.usecase.GetCompletionRateUseCase
import com.habittracker.app.domain.usecase.GetCurrentStreakUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class HabitStats(
    val habit: Habit,
    val currentStreak: Int,
    val bestStreak: Int,
    val rate7Day: Int,
    val completions: Map<String, Boolean>
)

data class StatsUiState(
    val selectedTab: Int = 0,
    val habits: List<HabitStats> = emptyList(),
    val totalCheckIns: Int = 0,
    val activeDays: Int = 0,
    val selectedMonth: YearMonth = YearMonth.now(),
    val selectedYear: Int = LocalDate.now().year,
    val monthlyCompletions: Map<String, Map<String, Boolean>> = emptyMap(),
    val loading: Boolean = true
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val repository: HabitRepository,
    private val getCurrentStreak: GetCurrentStreakUseCase,
    private val getBestStreak: GetBestStreakUseCase,
    private val getCompletionRate: GetCompletionRateUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(StatsUiState())
    val state = _state.asStateFlow()

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            repository.getHabitsFlow().collect { habits ->
                val totalCheckIns = repository.getTotalCompletionCount()
                val habitStats = habits.map { habit ->
                    val completions = repository.getAllCompletionsForHabit(habit.id)
                    HabitStats(
                        habit = habit,
                        currentStreak = getCurrentStreak(habit, completions),
                        bestStreak = getBestStreak(habit, completions),
                        rate7Day = getCompletionRate(habit, completions, 7),
                        completions = completions
                    )
                }
                val activeDays = calculateActiveDays(habits)
                _state.value = _state.value.copy(
                    habits = habitStats,
                    totalCheckIns = totalCheckIns,
                    activeDays = activeDays,
                    loading = false
                )
                loadMonthlyData()
            }
        }
    }

    private suspend fun calculateActiveDays(habits: List<Habit>): Int {
        val today = LocalDate.now()
        val start = today.minusYears(3)
        val allCompletions = repository.getCompletionsBetween(
            start.format(dateFormatter),
            today.format(dateFormatter)
        )
        return allCompletions.count { (_, habitMap) -> habitMap.any { it.value } }
    }

    private suspend fun loadMonthlyData() {
        val month = _state.value.selectedMonth
        val start = month.atDay(1).format(dateFormatter)
        val end = month.atEndOfMonth().format(dateFormatter)
        val completions = repository.getCompletionsBetween(start, end)
        _state.value = _state.value.copy(monthlyCompletions = completions)
    }

    fun selectTab(tab: Int) {
        _state.value = _state.value.copy(selectedTab = tab)
    }

    fun previousMonth() {
        val newMonth = _state.value.selectedMonth.minusMonths(1)
        _state.value = _state.value.copy(selectedMonth = newMonth)
        viewModelScope.launch { loadMonthlyDataForMonth(newMonth) }
    }

    fun nextMonth() {
        val now = YearMonth.now()
        if (_state.value.selectedMonth < now) {
            val newMonth = _state.value.selectedMonth.plusMonths(1)
            _state.value = _state.value.copy(selectedMonth = newMonth)
            viewModelScope.launch { loadMonthlyDataForMonth(newMonth) }
        }
    }

    fun previousYear() {
        _state.value = _state.value.copy(selectedYear = _state.value.selectedYear - 1)
    }

    fun nextYear() {
        if (_state.value.selectedYear < LocalDate.now().year) {
            _state.value = _state.value.copy(selectedYear = _state.value.selectedYear + 1)
        }
    }

    private suspend fun loadMonthlyDataForMonth(month: YearMonth) {
        val start = month.atDay(1).format(dateFormatter)
        val end = month.atEndOfMonth().format(dateFormatter)
        val completions = repository.getCompletionsBetween(start, end)
        _state.value = _state.value.copy(monthlyCompletions = completions)
    }
}
