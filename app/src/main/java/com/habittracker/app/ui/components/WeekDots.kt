package com.habittracker.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habittracker.app.ui.theme.Emerald500
import com.habittracker.app.ui.theme.Indigo600
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
private val dayLetters = listOf("S", "M", "T", "W", "T", "F", "S")

@Composable
fun WeekDots(
    completions: Map<String, Boolean>,
    scheduledDays: List<String>,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val days = (6 downTo 0).map { today.minusDays(it.toLong()) }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        days.forEach { date ->
            val dateStr = date.format(formatter)
            val dayOfWeek = date.dayOfWeek.value % 7
            val letter = dayLetters[dayOfWeek]
            val dayName = com.habittracker.app.Constants.DAY_NAMES[date.dayOfWeek.value - 1]
            val isScheduled = dayName in scheduledDays
            val isDone = completions[dateStr] == true
            val isToday = date == today
            val isPast = date.isBefore(today)

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = letter,
                    fontSize = 9.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(2.dp))
                DotIndicator(isScheduled, isDone, isToday, isPast)
            }
        }
    }
}

@Composable
private fun DotIndicator(isScheduled: Boolean, isDone: Boolean, isToday: Boolean, isPast: Boolean) {
    val size = 18.dp
    when {
        !isScheduled -> Box(
            Modifier.size(size).clip(CircleShape).background(Color(0xFFE2E8F0))
        )
        isDone -> Box(
            Modifier.size(size).clip(CircleShape).background(Emerald500),
            contentAlignment = Alignment.Center
        ) {
            Text("✓", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }
        isToday -> Box(
            Modifier.size(size).clip(CircleShape)
                .border(2.dp, Indigo600, CircleShape)
                .background(Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Box(Modifier.size(5.dp).clip(CircleShape).background(Indigo600))
        }
        isPast -> Box(
            Modifier.size(size).clip(CircleShape).background(Color(0xFFFEE2E2)),
            contentAlignment = Alignment.Center
        ) {
            Text("✕", fontSize = 8.sp, color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
        }
        else -> Box(
            Modifier.size(size).clip(CircleShape).background(Color(0xFFF1F5F9))
        )
    }
}
