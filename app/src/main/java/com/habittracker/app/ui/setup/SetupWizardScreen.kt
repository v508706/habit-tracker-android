package com.habittracker.app.ui.setup

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.habittracker.app.Constants
import com.habittracker.app.ui.theme.Indigo600
import com.habittracker.app.ui.theme.Violet600
import com.habittracker.app.ui.theme.Purple700
import android.graphics.Color as AndroidColor

@Composable
fun SetupWizardScreen(
    onSetupComplete: () -> Unit,
    viewModel: SetupViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.done) {
        if (state.done) onSetupComplete()
    }

    val gradient = Brush.linearGradient(listOf(Indigo600, Violet600, Purple700))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // Progress bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(3) { i ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (i <= state.step) Color.White
                            else Color.White.copy(alpha = 0.3f)
                        )
                )
            }
        }

        when (state.step) {
            0 -> StepName(state, viewModel)
            1 -> StepHabits(state, viewModel)
            2 -> StepNotifications(state, viewModel)
        }
    }
}

@Composable
private fun StepName(state: SetupUiState, viewModel: SetupViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        Brush.linearGradient(listOf(Indigo600, Violet600)),
                        RoundedCornerShape(20.dp)
                    ),
                contentAlignment = Alignment.Center
            ) { Text("👋", fontSize = 36.sp) }
        }
        item {
            Text("Welcome!", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color(0xFF1E293B))
        }
        item {
            Text(
                "Build better habits, one day at a time.\nLet's start with your profile.",
                fontSize = 15.sp, color = Color(0xFF64748B),
                lineHeight = 22.sp
            )
        }
        item {
            OutlinedTextField(
                value = state.userName,
                onValueChange = { viewModel.setUserName(it) },
                label = { Text("What should we call you?") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }
        item {
            Button(
                onClick = { viewModel.nextStep() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Indigo600)
            ) {
                Text("Continue →", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun StepHabits(state: SetupUiState, viewModel: SetupViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(Modifier.height(8.dp))
            Text("Your Habits", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color(0xFF1E293B))
            Text(
                "Add the routines you want to track.",
                fontSize = 14.sp, color = Color(0xFF64748B)
            )
        }
        items(state.habits) { draft ->
            HabitDraftRow(draft, onDelete = { viewModel.removeHabit(draft.id) })
        }
        if (state.showForm) {
            item {
                HabitDraftForm(
                    draft = state.draft,
                    onUpdate = { viewModel.updateDraft(it) },
                    onAdd = { viewModel.addHabit() },
                    onCancel = { viewModel.hideForm() }
                )
            }
        } else {
            item {
                OutlinedButton(
                    onClick = { viewModel.showForm() },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = ButtonDefaults.outlinedButtonBorder
                ) {
                    Text("+ Add a Habit", color = Indigo600, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        item {
            Button(
                onClick = { viewModel.nextStep() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = state.habits.isNotEmpty() && !state.showForm,
                colors = ButtonDefaults.buttonColors(containerColor = Indigo600)
            ) {
                Text("Continue →", fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HabitDraftRow(draft: HabitDraft, onDelete: () -> Unit) {
    val color = runCatching {
        Color(AndroidColor.parseColor(draft.color))
    }.getOrDefault(Indigo600)
    val days = when {
        draft.days.size == 7 -> "Every day"
        draft.days.containsAll(listOf("Mon","Tue","Wed","Thu","Fri")) && draft.days.size == 5 -> "Weekdays"
        draft.days == listOf("Sat","Sun") -> "Weekends"
        else -> draft.days.joinToString(", ")
    }
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(40.dp).clip(CircleShape).background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) { Text(draft.emoji, fontSize = 20.sp) }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(draft.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text("$days · ${draft.time}", fontSize = 12.sp, color = Color.Gray)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray)
            }
        }
    }
}

@Composable
private fun HabitDraftForm(
    draft: HabitDraft,
    onUpdate: (HabitDraft) -> Unit,
    onAdd: () -> Unit,
    onCancel: () -> Unit
) {
    var showTimePicker by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("New Habit", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                IconButton(onClick = onCancel, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Cancel")
                }
            }

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
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (emoji == draft.emoji) Indigo600.copy(alpha = 0.15f) else Color.Transparent)
                            .clickable { onUpdate(draft.copy(emoji = emoji)) },
                        contentAlignment = Alignment.Center
                    ) { Text(emoji, fontSize = 18.sp) }
                }
            }

            // Name
            OutlinedTextField(
                value = draft.name,
                onValueChange = { onUpdate(draft.copy(name = it.take(40))) },
                label = { Text("Habit name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )

            // Color picker
            Text("Color", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Constants.HABIT_COLORS.forEach { hex ->
                    val c = runCatching { Color(AndroidColor.parseColor(hex)) }.getOrDefault(Indigo600)
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(c)
                            .then(
                                if (hex == draft.color)
                                    Modifier.border(3.dp, Color(0xFF1E293B), CircleShape)
                                else Modifier
                            )
                            .clickable { onUpdate(draft.copy(color = hex)) }
                    )
                }
            }

            // Day toggles
            Text("Repeat", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Constants.DAY_NAMES.forEach { day ->
                    val selected = day in draft.days
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (selected) Indigo600 else Color(0xFFF1F5F9))
                            .clickable {
                                val newDays = if (selected) draft.days - day else draft.days + day
                                onUpdate(draft.copy(days = Constants.DAY_NAMES.filter { it in newDays }))
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            day.take(1),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selected) Color.White else Color(0xFF475569)
                        )
                    }
                }
            }

            // Time
            Text("Reminder time", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
            OutlinedTextField(
                value = draft.time,
                onValueChange = { onUpdate(draft.copy(time = it)) },
                label = { Text("HH:mm") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp)) {
                    Text("Cancel")
                }
                Button(
                    onClick = onAdd,
                    modifier = Modifier.weight(1f),
                    enabled = draft.name.isNotBlank() && draft.days.isNotEmpty(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Indigo600)
                ) {
                    Text("Add Habit")
                }
            }
        }
    }
}

@Composable
private fun StepNotifications(state: SetupUiState, viewModel: SetupViewModel) {
    var permissionGranted by remember { mutableStateOf(false) }
    var permissionDenied by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionGranted = granted
        permissionDenied = !granted
    }

    val notifGradient = Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFEA580C)))

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(Modifier.height(16.dp)) }
        item {
            Box(
                Modifier.size(80.dp).background(notifGradient, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) { Text("🔔", fontSize = 36.sp) }
        }
        item { Text("Stay on Track", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color(0xFF1E293B)) }
        item { Text("Get notified at exactly the right time.", fontSize = 15.sp, color = Color(0xFF64748B)) }
        item {
            Button(
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        permissionGranted = true
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B))
            ) {
                Text("Enable Notifications", fontWeight = FontWeight.SemiBold, color = Color.White)
            }
        }
        if (permissionGranted) {
            item { Text("✅ Notifications enabled!", color = Color(0xFF16A34A), fontWeight = FontWeight.SemiBold) }
        }
        if (permissionDenied) {
            item { Text("Blocked — enable in Settings", color = Color(0xFFEF4444), fontWeight = FontWeight.SemiBold) }
        }
        item {
            Button(
                onClick = { viewModel.completeSetup() },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !state.saving,
                colors = ButtonDefaults.buttonColors(containerColor = Indigo600)
            ) {
                if (state.saving) CircularProgressIndicator(Modifier.size(20.dp), color = Color.White)
                else Text("🚀 Get Started!", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
