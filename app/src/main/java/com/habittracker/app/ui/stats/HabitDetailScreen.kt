package com.habittracker.app.ui.stats

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.habittracker.app.Constants
import com.habittracker.app.data.repository.HabitRepository
import com.habittracker.app.domain.model.Habit
import com.habittracker.app.domain.usecase.GetBestStreakUseCase
import com.habittracker.app.domain.usecase.GetCompletionRateUseCase
import com.habittracker.app.domain.usecase.GetCurrentStreakUseCase
import com.habittracker.app.ui.components.WeekDots
import com.habittracker.app.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

private val dtFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")

data class HabitDetailUiState(
    val habit: Habit? = null,
    val completions: Map<String, Boolean> = emptyMap(),
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val rate7Day: Int = 0,
    val rate30Day: Int = 0,
    val selectedTab: Int = 0,
    val selectedMonth: YearMonth = YearMonth.now(),
    val selectedYear: Int = LocalDate.now().year
)

@HiltViewModel
class HabitDetailViewModel @Inject constructor(
    private val repository: HabitRepository,
    private val getCurrentStreak: GetCurrentStreakUseCase,
    private val getBestStreak: GetBestStreakUseCase,
    private val getCompletionRate: GetCompletionRateUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val habitId: String = savedStateHandle.get<String>("habitId") ?: ""
    private val _state = MutableStateFlow(HabitDetailUiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val habits = repository.getHabits()
            val habit = habits.firstOrNull { it.id == habitId } ?: return@launch
            val completions = repository.getAllCompletionsForHabit(habitId)
            _state.value = _state.value.copy(
                habit = habit,
                completions = completions,
                currentStreak = getCurrentStreak(habit, completions),
                bestStreak = getBestStreak(habit, completions),
                rate7Day = getCompletionRate(habit, completions, 7),
                rate30Day = getCompletionRate(habit, completions, 30)
            )
        }
    }

    fun selectTab(tab: Int) { _state.value = _state.value.copy(selectedTab = tab) }
    fun previousMonth() {
        _state.value = _state.value.copy(selectedMonth = _state.value.selectedMonth.minusMonths(1))
    }
    fun nextMonth() {
        if (_state.value.selectedMonth < YearMonth.now())
            _state.value = _state.value.copy(selectedMonth = _state.value.selectedMonth.plusMonths(1))
    }
    fun previousYear() { _state.value = _state.value.copy(selectedYear = _state.value.selectedYear - 1) }
    fun nextYear() {
        if (_state.value.selectedYear < LocalDate.now().year)
            _state.value = _state.value.copy(selectedYear = _state.value.selectedYear + 1)
    }
}

@Composable
fun HabitDetailScreen(
    onBack: () -> Unit,
    viewModel: HabitDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val gradient = Brush.linearGradient(
        listOf(Indigo600, Violet600, Purple700),
        start = Offset.Zero, end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )
    val habit = state.habit ?: return

    val habitColor = runCatching { Color(AndroidColor.parseColor(habit.color)) }.getOrDefault(Indigo600)

    Column(Modifier.fillMaxSize().background(Color(0xFFF8FAFC))) {
        Column(Modifier.fillMaxWidth().background(gradient).padding(24.dp)) {
            TextButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                Spacer(Modifier.width(4.dp))
                Text("Back to Stats", color = Color.White, fontSize = 13.sp)
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(48.dp).clip(CircleShape).background(Color.White.copy(0.2f)),
                    contentAlignment = Alignment.Center
                ) { Text(habit.emoji, fontSize = 24.sp) }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(habit.name, fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.White)
                    Text(
                        when {
                            habit.days.size == 7 -> "Every day"
                            habit.days.containsAll(listOf("Mon","Tue","Wed","Thu","Fri")) && habit.days.size == 5 -> "Weekdays"
                            else -> habit.days.joinToString(", ")
                        },
                        color = Color.White.copy(0.7f), fontSize = 12.sp
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QuickStatCard("🔥 ${state.currentStreak}", "Streak",
                    listOf(Color(0xFFF97316), Rose500), Modifier.weight(1f))
                QuickStatCard("🏆 ${state.bestStreak}", "Best",
                    listOf(Amber500, Color(0xFFF59E0B)), Modifier.weight(1f))
                QuickStatCard("${state.rate7Day}%", "7 days",
                    listOf(Indigo600, Violet600), Modifier.weight(1f))
                QuickStatCard("${state.rate30Day}%", "30 days",
                    listOf(Color(0xFF0D9488), Color(0xFF10B981)), Modifier.weight(1f))
            }

            Spacer(Modifier.height(16.dp))
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color.White.copy(0.2f)),
            ) {
                listOf("📅 Monthly", "📆 Yearly").forEachIndexed { i, label ->
                    Box(
                        Modifier.weight(1f).clickable { viewModel.selectTab(i) }
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (state.selectedTab == i) Color.White else Color.Transparent)
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                            color = if (state.selectedTab == i) Indigo600 else Color.White)
                    }
                }
            }
        }

        when (state.selectedTab) {
            0 -> HabitDetailMonthly(state, viewModel, habit, habitColor)
            1 -> HabitDetailYearly(state, viewModel, habit, habitColor)
        }
    }
}

@Composable
private fun QuickStatCard(value: String, label: String, colors: List<Color>, modifier: Modifier) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color.White.copy(0.15f),
        modifier = modifier
    ) {
        Column(Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.White)
            Text(label, fontSize = 10.sp, color = Color.White.copy(0.7f))
        }
    }
}

@Composable
private fun HabitDetailMonthly(
    state: HabitDetailUiState,
    viewModel: HabitDetailViewModel,
    habit: Habit,
    habitColor: Color
) {
    val today = LocalDate.now()
    val month = state.selectedMonth

    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.previousMonth() }) { Icon(Icons.Default.ChevronLeft, null) }
                Text(
                    "${month.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${month.year}",
                    fontWeight = FontWeight.Bold, fontSize = 15.sp,
                    modifier = Modifier.weight(1f), textAlign = TextAlign.Center
                )
                IconButton(onClick = { viewModel.nextMonth() }, enabled = month < YearMonth.now()) {
                    Icon(Icons.Default.ChevronRight, null, tint = if (month < YearMonth.now()) Color.Unspecified else Color.LightGray)
                }
            }
        }

        item { HabitMonthCalendar(month, habit, state.completions, habitColor, today) }
        item {
            WeekDots(
                completions = state.completions,
                scheduledDays = habit.days,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun HabitMonthCalendar(
    month: YearMonth,
    habit: Habit,
    completions: Map<String, Boolean>,
    habitColor: Color,
    today: LocalDate
) {
    val firstDay = month.atDay(1)
    val daysInMonth = month.lengthOfMonth()
    val startOffset = firstDay.dayOfWeek.value % 7

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth()) {
                listOf("Su","Mo","Tu","We","Th","Fr","Sa").forEach { d ->
                    Text(d, Modifier.weight(1f), textAlign = TextAlign.Center,
                        fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                }
            }
            Spacer(Modifier.height(4.dp))

            val cells = mutableListOf<LocalDate?>()
            repeat(startOffset) { cells.add(null) }
            for (d in 1..daysInMonth) cells.add(month.atDay(d))
            while (cells.size % 7 != 0) cells.add(null)

            cells.chunked(7).forEach { week ->
                Row(Modifier.fillMaxWidth()) {
                    week.forEach { date ->
                        if (date == null) {
                            Box(Modifier.weight(1f).aspectRatio(1f))
                        } else {
                            val dateStr = date.format(dtFmt)
                            val dayName = Constants.DAY_NAMES[date.dayOfWeek.value - 1]
                            val isScheduled = dayName in habit.days
                            val isDone = completions[dateStr] == true
                            val isFuture = date.isAfter(today)
                            val isToday = date == today

                            val bg = when {
                                !isScheduled -> Color(0xFFE2E8F0)
                                isFuture -> Color(0xFFF1F5F9)
                                isDone -> habitColor
                                else -> Color(0xFFFEE2E2)
                            }

                            Box(
                                Modifier.weight(1f).aspectRatio(1f).padding(2.dp)
                                    .clip(CircleShape).background(bg)
                                    .then(if (isToday) Modifier.border(2.dp, Indigo600, CircleShape) else Modifier),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "${date.dayOfMonth}",
                                    fontSize = 11.sp,
                                    color = if (isDone && isScheduled) Color.White else Color(0xFF1E293B),
                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HabitDetailYearly(
    state: HabitDetailUiState,
    viewModel: HabitDetailViewModel,
    habit: Habit,
    habitColor: Color
) {
    val today = LocalDate.now()

    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.previousYear() }) { Icon(Icons.Default.ChevronLeft, null) }
                Text(
                    "${state.selectedYear}", fontWeight = FontWeight.Bold, fontSize = 18.sp,
                    modifier = Modifier.weight(1f), textAlign = TextAlign.Center
                )
                IconButton(onClick = { viewModel.nextYear() }, enabled = state.selectedYear < today.year) {
                    Icon(Icons.Default.ChevronRight, null,
                        tint = if (state.selectedYear < today.year) Color.Unspecified else Color.LightGray)
                }
            }
        }

        item {
            val months = (1..12).map { YearMonth.of(state.selectedYear, it) }
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxWidth().heightIn(max = 2000.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                userScrollEnabled = false
            ) {
                items(months) { month ->
                    if (!month.isAfter(YearMonth.now())) {
                        SingleHabitMiniMonth(month, habit, state.completions, habitColor, today)
                    } else {
                        Box(Modifier.fillMaxWidth().height(80.dp))
                    }
                }
            }
        }

        // Legend
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                LegendDot(habitColor, "Done")
                LegendDot(Color(0xFFFCA5A5), "Missed")
                LegendDot(Color(0xFFE2E8F0), "Rest day")
            }
            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun SingleHabitMiniMonth(
    month: YearMonth,
    habit: Habit,
    completions: Map<String, Boolean>,
    habitColor: Color,
    today: LocalDate
) {
    val firstDay = month.atDay(1)
    val daysInMonth = month.lengthOfMonth()
    val startOffset = firstDay.dayOfWeek.value % 7
    var scheduled = 0; var done = 0
    for (d in 1..daysInMonth) {
        val date = month.atDay(d)
        if (date.isAfter(today)) continue
        val dayName = Constants.DAY_NAMES[date.dayOfWeek.value - 1]
        if (dayName in habit.days) {
            scheduled++
            if (completions[date.format(dtFmt)] == true) done++
        }
    }
    val pct = if (scheduled == 0) null else done * 100 / scheduled
    val pctColor = when { pct == null -> Color.Gray; pct >= 100 -> Color(0xFF16A34A); pct >= 70 -> Color(0xFFD97706); else -> Color(0xFFEF4444) }

    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(1.dp)) {
        Column(Modifier.padding(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(month.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                pct?.let {
                    Surface(shape = RoundedCornerShape(50), color = pctColor.copy(0.1f)) {
                        Text("$it%", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = pctColor,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                    }
                }
            }
            Spacer(Modifier.height(4.dp))

            val cells = mutableListOf<LocalDate?>()
            repeat(startOffset) { cells.add(null) }
            for (d in 1..daysInMonth) cells.add(month.atDay(d))
            while (cells.size % 7 != 0) cells.add(null)

            cells.chunked(7).forEach { week ->
                Row(Modifier.fillMaxWidth()) {
                    week.forEach { date ->
                        if (date == null) {
                            Box(Modifier.weight(1f).aspectRatio(1f).padding(1.dp))
                        } else {
                            val isFuture = date.isAfter(today)
                            val isToday = date == today
                            val dayName = Constants.DAY_NAMES[date.dayOfWeek.value - 1]
                            val isScheduled = dayName in habit.days
                            val isDone = completions[date.format(dtFmt)] == true
                            val dotColor = when {
                                isFuture -> Color(0xFFF1F5F9)
                                !isScheduled -> Color(0xFFE2E8F0)
                                isDone -> habitColor
                                else -> Color(0xFFFCA5A5)
                            }
                            Box(
                                Modifier.weight(1f).aspectRatio(1f).padding(1.dp).clip(CircleShape)
                                    .background(dotColor)
                                    .then(if (isToday) Modifier.border(1.5.dp, Indigo600, CircleShape) else Modifier)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(4.dp))
        Text(label, fontSize = 11.sp, color = Color.Gray)
    }
}
