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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.habittracker.app.Constants
import com.habittracker.app.ui.components.StatCard
import com.habittracker.app.ui.components.WeekDots
import com.habittracker.app.ui.theme.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")

@Composable
fun StatsScreen(
    onHabitClick: (String) -> Unit,
    viewModel: StatsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val gradient = Brush.linearGradient(
        listOf(Indigo600, Violet600, Purple700),
        start = Offset.Zero, end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    Column(Modifier.fillMaxSize().background(Color(0xFFF8FAFC))) {
        // Header with tab selector
        Column(Modifier.fillMaxWidth().background(gradient).padding(24.dp)) {
            Text("Statistics", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White)
            Spacer(Modifier.height(16.dp))
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(0.2f)),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("Overview", "Monthly", "Yearly").forEachIndexed { i, label ->
                    Box(
                        Modifier.weight(1f).clickable { viewModel.selectTab(i) }
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (state.selectedTab == i) Color.White else Color.Transparent)
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            label,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (state.selectedTab == i) Indigo600 else Color.White
                        )
                    }
                }
            }
        }

        when (state.selectedTab) {
            0 -> OverviewTab(state, onHabitClick)
            1 -> MonthlyTab(state, viewModel, onHabitClick)
            2 -> YearlyTab(state, viewModel)
        }
    }
}

// ─── Overview Tab ────────────────────────────────────────────────────────────

@Composable
private fun OverviewTab(state: StatsUiState, onHabitClick: (String) -> Unit) {
    val maxCurrentStreak = state.habits.maxOfOrNull { it.currentStreak } ?: 0
    val avgRate = if (state.habits.isEmpty()) 0
    else state.habits.sumOf { it.rate7Day } / state.habits.size

    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Summary 2x2 grid
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(
                    label = "Active Habits",
                    value = "${state.habits.size}",
                    gradient = Brush.linearGradient(listOf(Indigo600, Violet600)),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Total Check-ins",
                    value = "${state.totalCheckIns}",
                    gradient = Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF0D9488))),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(
                    label = "Avg 7-Day Rate",
                    value = "$avgRate%",
                    gradient = Brush.linearGradient(listOf(Amber500, Color(0xFFF97316))),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Best Streak Now",
                    value = "🔥 $maxCurrentStreak",
                    gradient = Brush.linearGradient(listOf(Rose500, Color(0xFFE11D48))),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Active days card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("📅", fontSize = 28.sp)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "${state.activeDays} active days with at least one check-in",
                        fontSize = 14.sp, color = Color(0xFF475569), fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Per-habit list
        items(state.habits, key = { it.habit.id }) { hs ->
            HabitStatRow(hs, onClick = { onHabitClick(hs.habit.id) })
        }

        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun HabitStatRow(hs: HabitStats, onClick: () -> Unit) {
    val color = runCatching { Color(AndroidColor.parseColor(hs.habit.color)) }.getOrDefault(Indigo600)

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(40.dp).clip(CircleShape).background(color.copy(0.15f)),
                    contentAlignment = Alignment.Center
                ) { Text(hs.habit.emoji, fontSize = 20.sp) }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(hs.habit.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text(
                        hs.habit.days.joinToString(", "),
                        fontSize = 11.sp, color = Color.Gray
                    )
                }
                Text("›", fontSize = 20.sp, color = Color.Gray)
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Chip("🔥 ${hs.currentStreak}", Color(0xFFFFF7ED), Color(0xFFEA580C))
                Chip("🏆 ${hs.bestStreak}", Color(0xFFFFFBEB), Color(0xFFD97706))
                Chip("${hs.rate7Day}% 7d", Color(0xFFEEF2FF), Indigo600)
            }
            Spacer(Modifier.height(8.dp))
            Box(
                Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(50))
                    .background(Color(0xFFF1F5F9))
            ) {
                Box(
                    Modifier.fillMaxWidth(hs.rate7Day / 100f).fillMaxHeight()
                        .clip(RoundedCornerShape(50)).background(color)
                )
            }
            Spacer(Modifier.height(10.dp))
            WeekDots(
                completions = hs.completions,
                scheduledDays = hs.habit.days,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun Chip(text: String, bg: Color, textColor: Color) {
    Surface(shape = RoundedCornerShape(50), color = bg) {
        Text(
            text, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

// ─── Monthly Tab ─────────────────────────────────────────────────────────────

@Composable
private fun MonthlyTab(state: StatsUiState, viewModel: StatsViewModel, onHabitClick: (String) -> Unit) {
    val month = state.selectedMonth
    val now = YearMonth.now()

    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                IconButton(onClick = { viewModel.previousMonth() }) {
                    Icon(Icons.Default.ChevronLeft, "Prev")
                }
                Text(
                    "${month.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${month.year}",
                    fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                IconButton(onClick = { viewModel.nextMonth() }, enabled = month < now) {
                    Icon(Icons.Default.ChevronRight, "Next", tint = if (month < now) Color.Unspecified else Color.LightGray)
                }
            }
        }

        item { AllHabitsCalendar(month, state.monthlyCompletions, state.habits) }

        items(state.habits, key = { it.habit.id }) { hs ->
            HabitMonthRow(hs, month, state.monthlyCompletions, onClick = { onHabitClick(hs.habit.id) })
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun AllHabitsCalendar(
    month: YearMonth,
    completions: Map<String, Map<String, Boolean>>,
    habits: List<HabitStats>
) {
    val today = LocalDate.now()
    val firstDay = month.atDay(1)
    val daysInMonth = month.lengthOfMonth()
    // Sunday=0 offset
    val startOffset = firstDay.dayOfWeek.value % 7

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth()) {
                listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa").forEach { d ->
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
                            val dateStr = date.format(dateFmt)
                            val dayName = Constants.DAY_NAMES[date.dayOfWeek.value - 1]
                            val scheduled = habits.filter { dayName in it.habit.days }
                            val done = scheduled.count { completions[dateStr]?.get(it.habit.id) == true }
                            val total = scheduled.size
                            val isFuture = date.isAfter(today)
                            val isToday = date == today

                            val bg = when {
                                isFuture -> Color(0xFFF1F5F9)
                                total == 0 -> Color(0xFFF1F5F9)
                                done == total -> Color(0xFF10B981)
                                done.toFloat() / total >= 0.5f -> Color(0xFF86EFAC)
                                done > 0 -> Color(0xFFFDE68A)
                                else -> Color(0xFFFEE2E2)
                            }
                            val textColor = when {
                                done == total && total > 0 -> Color.White
                                else -> Color(0xFF1E293B)
                            }

                            Box(
                                Modifier.weight(1f).aspectRatio(1f).padding(2.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(bg)
                                    .then(if (isToday) Modifier.border(2.dp, Indigo600, RoundedCornerShape(6.dp)) else Modifier),
                                contentAlignment = Alignment.Center
                            ) {
                                if (done == total && total > 0)
                                    Text("✓", fontSize = 10.sp, color = textColor, fontWeight = FontWeight.Bold)
                                else if (total > 0 && !isFuture)
                                    Text("$done/$total", fontSize = 9.sp, color = textColor, fontWeight = FontWeight.Bold)
                                else
                                    Text("${date.dayOfMonth}", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HabitMonthRow(
    hs: HabitStats,
    month: YearMonth,
    completions: Map<String, Map<String, Boolean>>,
    onClick: () -> Unit
) {
    val today = LocalDate.now()
    val color = runCatching { Color(AndroidColor.parseColor(hs.habit.color)) }.getOrDefault(Indigo600)

    var scheduled = 0
    var done = 0
    for (d in 1..month.lengthOfMonth()) {
        val date = month.atDay(d)
        if (date.isAfter(today)) continue
        val dayName = Constants.DAY_NAMES[date.dayOfWeek.value - 1]
        if (dayName in hs.habit.days) {
            scheduled++
            if (completions[date.format(dateFmt)]?.get(hs.habit.id) == true) done++
        }
    }
    val pct = if (scheduled == 0) 0 else done * 100 / scheduled
    val pctColor = when { pct >= 100 -> Color(0xFF16A34A); pct >= 50 -> Color(0xFFD97706); else -> Color(0xFFEF4444) }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(38.dp).clip(CircleShape).background(color.copy(0.15f)),
                contentAlignment = Alignment.Center) { Text(hs.habit.emoji, fontSize = 18.sp) }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(hs.habit.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text("$done of $scheduled days done", fontSize = 11.sp, color = Color.Gray)
                Spacer(Modifier.height(4.dp))
                Box(
                    Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(50))
                        .background(Color(0xFFF1F5F9))
                ) {
                    Box(
                        Modifier.fillMaxWidth(pct / 100f).fillMaxHeight()
                            .clip(RoundedCornerShape(50)).background(color)
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            Surface(shape = RoundedCornerShape(50), color = pctColor.copy(0.1f)) {
                Text("$pct%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = pctColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
            }
        }
    }
}

// ─── Yearly Tab ──────────────────────────────────────────────────────────────

@Composable
private fun YearlyTab(state: StatsUiState, viewModel: StatsViewModel) {
    val today = LocalDate.now()
    val year = state.selectedYear

    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.previousYear() }) {
                    Icon(Icons.Default.ChevronLeft, "Prev")
                }
                Text(
                    "$year", fontWeight = FontWeight.Bold, fontSize = 18.sp,
                    modifier = Modifier.weight(1f), textAlign = TextAlign.Center
                )
                IconButton(onClick = { viewModel.nextYear() }, enabled = year < today.year) {
                    Icon(Icons.Default.ChevronRight, "Next",
                        tint = if (year < today.year) Color.Unspecified else Color.LightGray)
                }
            }
        }

        // Year summary chips
        val yearCheckIns = countYearCheckIns(state.habits, year)
        val yearRate = calculateYearRate(state.habits, year, today)
        val yearBestStreak = state.habits.maxOfOrNull { it.bestStreak } ?: 0

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFECFDF5), modifier = Modifier.weight(1f)) {
                    Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$yearCheckIns", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color(0xFF059669))
                        Text("Check-ins", fontSize = 10.sp, color = Color.Gray)
                    }
                }
                Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFEEF2FF), modifier = Modifier.weight(1f)) {
                    Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$yearRate%", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Indigo600)
                        Text("Completion", fontSize = 10.sp, color = Color.Gray)
                    }
                }
                Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFFFF7ED), modifier = Modifier.weight(1f)) {
                    Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🔥 $yearBestStreak", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFFEA580C))
                        Text("Best Streak", fontSize = 10.sp, color = Color.Gray)
                    }
                }
            }
        }

        // 12 mini-month grids in 2-column grid
        item {
            val months = (1..12).map { YearMonth.of(year, it) }
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxWidth().heightIn(max = 2000.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                userScrollEnabled = false
            ) {
                items(months) { month ->
                    MiniMonthGrid(month, state.habits, today)
                }
            }
        }

        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun MiniMonthGrid(month: YearMonth, habits: List<HabitStats>, today: LocalDate) {
    if (month.isAfter(YearMonth.now())) return

    val firstDay = month.atDay(1)
    val daysInMonth = month.lengthOfMonth()
    val startOffset = firstDay.dayOfWeek.value % 7

    var scheduled = 0
    var done = 0
    for (d in 1..daysInMonth) {
        val date = month.atDay(d)
        if (date.isAfter(today)) continue
        val dayName = Constants.DAY_NAMES[date.dayOfWeek.value - 1]
        habits.forEach { hs ->
            if (dayName in hs.habit.days) {
                scheduled++
                if (hs.completions[date.format(dateFmt)] == true) done++
            }
        }
    }
    val pct = if (scheduled == 0) null else done * 100 / scheduled
    val pctColor = when { pct == null -> Color.Gray; pct >= 100 -> Color(0xFF16A34A); pct >= 70 -> Color(0xFFD97706); else -> Color(0xFFEF4444) }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(Modifier.padding(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    month.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                    fontSize = 11.sp, fontWeight = FontWeight.Bold
                )
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
                            val dateStr = date.format(dateFmt)
                            val dayName = Constants.DAY_NAMES[date.dayOfWeek.value - 1]
                            val anyScheduled = habits.any { dayName in it.habit.days }
                            val anyDone = habits.any { hs ->
                                dayName in hs.habit.days && hs.completions[dateStr] == true
                            }

                            val dotColor = when {
                                isFuture -> Color(0xFFF1F5F9)
                                !anyScheduled -> Color(0xFFE2E8F0)
                                anyDone -> Color(0xFF10B981)
                                else -> Color(0xFFFCA5A5)
                            }
                            Box(
                                Modifier.weight(1f).aspectRatio(1f).padding(1.dp)
                                    .clip(CircleShape)
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

private fun countYearCheckIns(habits: List<HabitStats>, year: Int): Int {
    var count = 0
    val prefix = "$year-"
    habits.forEach { hs ->
        hs.completions.forEach { (date, done) ->
            if (date.startsWith(prefix) && done) count++
        }
    }
    return count
}

private fun calculateYearRate(habits: List<HabitStats>, year: Int, today: LocalDate): Int {
    var scheduled = 0; var done = 0
    val start = LocalDate.of(year, 1, 1)
    val end = if (today.year == year) today else LocalDate.of(year, 12, 31)
    var d = start
    while (!d.isAfter(end)) {
        val dayName = Constants.DAY_NAMES[d.dayOfWeek.value - 1]
        val dateStr = d.format(dateFmt)
        habits.forEach { hs ->
            if (dayName in hs.habit.days) {
                scheduled++
                if (hs.completions[dateStr] == true) done++
            }
        }
        d = d.plusDays(1)
    }
    return if (scheduled == 0) 0 else done * 100 / scheduled
}
