package com.habittracker.app.ui.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habittracker.app.Constants
import com.habittracker.app.data.repository.HabitRepository
import com.habittracker.app.domain.model.Habit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class HabitFormState(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val emoji: String = "🎯",
    val color: String = "#6366f1",
    val days: List<String> = listOf("Mon", "Tue", "Wed", "Thu", "Fri"),
    val time: String = "08:00",
    val isEditing: Boolean = false
)

data class HabitManagerUiState(
    val habits: List<Habit> = emptyList(),
    val showBottomSheet: Boolean = false,
    val formState: HabitFormState = HabitFormState(),
    val deleteConfirmHabitId: String? = null
)

@HiltViewModel
class HabitManagerViewModel @Inject constructor(
    private val repository: HabitRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HabitManagerUiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getHabitsFlow().collect { habits ->
                _state.value = _state.value.copy(habits = habits)
            }
        }
    }

    fun showAddSheet() {
        _state.value = _state.value.copy(
            showBottomSheet = true,
            formState = HabitFormState()
        )
    }

    fun showEditSheet(habit: Habit) {
        _state.value = _state.value.copy(
            showBottomSheet = true,
            formState = HabitFormState(
                id = habit.id,
                name = habit.name,
                emoji = habit.emoji,
                color = habit.color,
                days = habit.days,
                time = habit.time,
                isEditing = true
            )
        )
    }

    fun hideSheet() {
        _state.value = _state.value.copy(showBottomSheet = false)
    }

    fun updateForm(form: HabitFormState) {
        _state.value = _state.value.copy(formState = form)
    }

    fun saveHabit() {
        val form = _state.value.formState
        if (form.name.isBlank() || form.days.isEmpty()) return
        viewModelScope.launch {
            repository.saveHabit(
                Habit(
                    id = form.id,
                    name = form.name,
                    emoji = form.emoji,
                    color = form.color,
                    days = form.days,
                    time = form.time
                )
            )
            _state.value = _state.value.copy(showBottomSheet = false)
        }
    }

    fun confirmDelete(habitId: String) {
        _state.value = _state.value.copy(deleteConfirmHabitId = habitId)
    }

    fun cancelDelete() {
        _state.value = _state.value.copy(deleteConfirmHabitId = null)
    }

    fun deleteHabit(habitId: String) {
        viewModelScope.launch {
            repository.deleteHabit(habitId)
            _state.value = _state.value.copy(deleteConfirmHabitId = null)
        }
    }
}
