package com.habittracker.app.ui.habits

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.habittracker.app.Constants
import com.habittracker.app.domain.model.Habit
import com.habittracker.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitManagerScreen(viewModel: HabitManagerViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val gradient = Brush.linearGradient(
        listOf(Indigo600, Violet600, Purple700),
        start = Offset.Zero, end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    Box(Modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxSize().background(Color(0xFFF8FAFC))) {
            item {
                Box(Modifier.fillMaxWidth().background(gradient).padding(24.dp)) {
                    Column {
                        Text("My Habits", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White)
                        Text("${state.habits.size} habits", color = Color.White.copy(0.7f), fontSize = 13.sp)
                    }
                }
            }

            if (state.habits.isEmpty()) {
                item {
                    Column(
                        Modifier.fillMaxWidth().padding(48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🌱", fontSize = 48.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("No habits yet", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                        Text("Start building your routines!", color = Color(0xFF64748B), fontSize = 14.sp)
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.showAddSheet() },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Indigo600)
                        ) { Text("+ Add First Habit") }
                    }
                }
            } else {
                items(state.habits, key = { it.id }) { habit ->
                    HabitRow(
                        habit = habit,
                        onEdit = { viewModel.showEditSheet(habit) },
                        onDelete = { viewModel.confirmDelete(habit.id) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
                item {
                    OutlinedButton(
                        onClick = { viewModel.showAddSheet() },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("+ Add Another Habit", color = Indigo600, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(80.dp))
                }
            }
        }

        // Bottom Sheet
        if (state.showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.hideSheet() },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                HabitFormSheet(
                    form = state.formState,
                    onUpdate = { viewModel.updateForm(it) },
                    onSave = { viewModel.saveHabit() },
                    onCancel = { viewModel.hideSheet() }
                )
            }
        }

        // Delete dialog
        state.deleteConfirmHabitId?.let { habitId ->
            AlertDialog(
                onDismissRequest = { viewModel.cancelDelete() },
                title = { Text("Delete this habit?") },
                text = { Text("Your completion history will be kept.") },
                confirmButton = {
                    TextButton(onClick = { viewModel.deleteHabit(habitId) }) {
                        Text("Delete", color = Color(0xFFEF4444))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.cancelDelete() }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
private fun HabitRow(habit: Habit, onEdit: () -> Unit, onDelete: () -> Unit, modifier: Modifier = Modifier) {
    val color = runCatching { Color(AndroidColor.parseColor(habit.color)) }.getOrDefault(Indigo600)
    val days = when {
        habit.days.size == 7 -> "Every day"
        habit.days.containsAll(listOf("Mon","Tue","Wed","Thu","Fri")) && habit.days.size == 5 -> "Weekdays"
        habit.days.toSet() == setOf("Sat","Sun") -> "Weekends"
        else -> habit.days.joinToString(", ")
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(44.dp).clip(CircleShape).background(color.copy(0.15f)),
                contentAlignment = Alignment.Center
            ) { Text(habit.emoji, fontSize = 22.sp) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(habit.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text("$days · ${habit.time}", fontSize = 12.sp, color = Color.Gray)
            }
            Box(Modifier.size(10.dp).clip(CircleShape).background(color))
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Edit, "Edit", tint = Indigo600, modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Delete, "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun HabitFormSheet(
    form: HabitFormState,
    onUpdate: (HabitFormState) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        Modifier.padding(horizontal = 20.dp).padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            if (form.isEditing) "Edit Habit" else "New Habit",
            fontWeight = FontWeight.Bold, fontSize = 18.sp
        )

        // Emoji picker
        Text("Emoji", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.height(140.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(Constants.HABIT_EMOJIS) { emoji ->
                Box(
                    Modifier.size(36.dp).clip(RoundedCornerShape(8.dp))
                        .background(if (emoji == form.emoji) Indigo600.copy(0.15f) else Color.Transparent)
                        .clickable { onUpdate(form.copy(emoji = emoji)) },
                    contentAlignment = Alignment.Center
                ) { Text(emoji, fontSize = 18.sp) }
            }
        }

        OutlinedTextField(
            value = form.name,
            onValueChange = { onUpdate(form.copy(name = it.take(40))) },
            label = { Text("Habit name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(10.dp)
        )

        // Color picker
        Text("Color", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(Constants.HABIT_COLORS) { hex ->
                val c = runCatching { Color(AndroidColor.parseColor(hex)) }.getOrDefault(Indigo600)
                Box(
                    Modifier.size(30.dp).clip(CircleShape).background(c)
                        .then(if (hex == form.color) Modifier.border(3.dp, Color(0xFF1E293B), CircleShape) else Modifier)
                        .clickable { onUpdate(form.copy(color = hex)) }
                )
            }
        }

        // Day toggles
        Text("Repeat", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Constants.DAY_NAMES.forEach { day ->
                val selected = day in form.days
                Box(
                    Modifier.size(38.dp).clip(CircleShape)
                        .background(if (selected) Indigo600 else Color(0xFFF1F5F9))
                        .clickable {
                            val newDays = if (selected) form.days - day else form.days + day
                            onUpdate(form.copy(days = Constants.DAY_NAMES.filter { it in newDays }))
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        day.take(1), fontSize = 11.sp, fontWeight = FontWeight.Bold,
                        color = if (selected) Color.White else Color(0xFF475569)
                    )
                }
            }
        }

        OutlinedTextField(
            value = form.time,
            onValueChange = { onUpdate(form.copy(time = it)) },
            label = { Text("Reminder time (HH:mm)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(10.dp)
        )

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp)) {
                Text("Cancel")
            }
            Button(
                onClick = onSave,
                modifier = Modifier.weight(1f),
                enabled = form.name.isNotBlank() && form.days.isNotEmpty(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Indigo600)
            ) {
                Text(if (form.isEditing) "Save Changes" else "Add Habit")
            }
        }
    }
}
