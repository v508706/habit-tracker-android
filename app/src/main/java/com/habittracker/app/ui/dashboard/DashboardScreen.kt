package com.habittracker.app.ui.dashboard

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.habittracker.app.ui.components.HabitCard
import com.habittracker.app.ui.components.ProgressRing
import com.habittracker.app.ui.theme.*

@Composable
fun DashboardScreen(viewModel: DashboardViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val gradient = Brush.linearGradient(
        listOf(Indigo600, Violet600, Purple700),
        start = Offset.Zero,
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF8FAFC))
    ) {
        // Header
        item {
            Box(modifier = Modifier.fillMaxWidth().background(gradient).padding(24.dp)) {
                Column {
                    Text(
                        state.dateLabel,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        "Hello, ${state.userName}! 👋",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        "${state.scheduledCount} habits scheduled",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Spacer(Modifier.height(16.dp))

                    // Progress card
                    val percent = if (state.scheduledCount > 0)
                        state.doneCount.toFloat() / state.scheduledCount else 0f
                    val motivational = when {
                        percent >= 1f -> "🎉 All done! You crushed it!"
                        percent >= 0.75f -> "🔥 Almost there! Keep going!"
                        percent >= 0.5f -> "💪 Halfway there! Great momentum!"
                        state.doneCount > 0 -> "⚡ Good start! Keep it up!"
                        else -> "✨ Ready to build great habits today?"
                    }

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f)),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ProgressRing(percent = percent, size = 80.dp, strokeWidth = 8.dp)
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(
                                    "${state.doneCount}/${state.scheduledCount} completed today",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                                Text(motivational, color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }

        // Streak nudges
        state.streakNudges.forEach { nudge ->
            item(key = "nudge_${nudge.habit.id}") {
                val nudgeColor = when {
                    nudge.streak >= 30 -> listOf(Color(0xFFEF4444), Color(0xFFDC2626))
                    nudge.streak >= 7 -> listOf(Color(0xFFF97316), Color(0xFFEA580C))
                    else -> listOf(Color(0xFFF59E0B), Color(0xFFD97706))
                }
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Box(
                        Modifier.background(Brush.linearGradient(nudgeColor)).padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(nudge.habit.emoji, fontSize = 24.sp)
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(nudge.habit.name, fontWeight = FontWeight.Bold, color = Color.White)
                                Text(
                                    "🔥 ${nudge.streak} day streak — don't break it!",
                                    color = Color.White.copy(0.9f), fontSize = 12.sp
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Button(
                                    onClick = { viewModel.toggleCompletion(nudge.habit.id) },
                                    modifier = Modifier.height(32.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                    contentPadding = PaddingValues(horizontal = 12.dp)
                                ) {
                                    Text("Do it ✓", color = Color(0xFFEA580C), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(Modifier.height(4.dp))
                                TextButton(
                                    onClick = { viewModel.dismissNudge(nudge.habit.id) },
                                    modifier = Modifier.height(24.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp)
                                ) {
                                    Text("Dismiss", color = Color.White.copy(0.7f), fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Habit list
        if (state.habits.isEmpty()) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🌟", fontSize = 48.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("Free day!", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                    val dayName = java.time.LocalDate.now()
                        .dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.getDefault())
                    Text(
                        "No habits scheduled for $dayName.",
                        color = Color(0xFF64748B), fontSize = 14.sp
                    )
                }
            }
        } else {
            items(state.habits, key = { it.id }) { habit ->
                HabitCard(
                    habit = habit,
                    done = state.completions[habit.id] == true,
                    streak = state.streaks[habit.id] ?: 0,
                    onToggle = { viewModel.toggleCompletion(habit.id) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            // All done banner
            if (state.scheduledCount > 0 && state.doneCount == state.scheduledCount) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF10B981), Color(0xFF059669))
                                    )
                                )
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "🏆 Perfect day! You completed all your habits.",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(80.dp)) }
    }
}
