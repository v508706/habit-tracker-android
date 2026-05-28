package com.habittracker.app.domain.usecase

import com.habittracker.app.Constants.DAY_NAMES
import com.habittracker.app.domain.model.Habit
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

private fun LocalDate.toDateString() = format(formatter)
private fun String.toLocalDate(): LocalDate = LocalDate.parse(this, formatter)
private fun LocalDate.dayName(): String = DAY_NAMES[(dayOfWeek.value % 7)]

// dayOfWeek: Mon=1..Sun=7, we want Mon=0..Sun=6 for DAY_NAMES index
// Actually Java DayOfWeek: Mon=1, Tue=2, ..., Sat=6, Sun=7
// DAY_NAMES = ["Mon","Tue","Wed","Thu","Fri","Sat","Sun"] indices 0..6
// So index = dayOfWeek.value - 1
private fun LocalDate.habitDayName(): String = DAY_NAMES[dayOfWeek.value - 1]

class GetCurrentStreakUseCase @Inject constructor() {
    operator fun invoke(
        habit: Habit,
        completions: Map<String, Boolean>
    ): Int {
        val today = LocalDate.now()
        var streak = 0

        // Walk backwards from yesterday
        var day = today.minusDays(1)
        val earliest = today.minusYears(3)
        while (!day.isBefore(earliest)) {
            val dayName = day.habitDayName()
            if (dayName in habit.days) {
                if (completions[day.toDateString()] == true) {
                    streak++
                } else {
                    break
                }
            }
            day = day.minusDays(1)
        }

        // Also count today if scheduled and done
        if (today.habitDayName() in habit.days && completions[today.toDateString()] == true) {
            streak++
        }

        return streak
    }
}

class GetBestStreakUseCase @Inject constructor() {
    operator fun invoke(
        habit: Habit,
        completions: Map<String, Boolean>
    ): Int {
        val today = LocalDate.now()
        val start = today.minusYears(3)
        var best = 0
        var current = 0
        var day = start

        while (!day.isAfter(today)) {
            val dayName = day.habitDayName()
            if (dayName in habit.days) {
                val done = completions[day.toDateString()] == true
                val isToday = day == today
                if (done) {
                    current++
                    if (current > best) best = current
                } else if (!isToday) {
                    current = 0
                }
            }
            day = day.plusDays(1)
        }

        return best
    }
}

class GetCompletionRateUseCase @Inject constructor() {
    operator fun invoke(
        habit: Habit,
        completions: Map<String, Boolean>,
        days: Int
    ): Int {
        val today = LocalDate.now()
        var scheduled = 0
        var done = 0

        for (i in 0 until days) {
            val day = today.minusDays(i.toLong())
            val dayName = day.habitDayName()
            if (dayName in habit.days) {
                scheduled++
                if (completions[day.toDateString()] == true) done++
            }
        }

        return if (scheduled == 0) 0 else Math.round(done.toFloat() / scheduled * 100)
    }
}
