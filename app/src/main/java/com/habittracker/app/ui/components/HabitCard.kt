package com.habittracker.app.ui.components

import android.graphics.Color as AndroidColor
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habittracker.app.domain.model.Habit
import com.habittracker.app.ui.theme.Emerald500
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HabitCard(
    habit: Habit,
    done: Boolean,
    streak: Int,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    var animating by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (animating) 1.15f else 1.0f,
        animationSpec = tween(150),
        label = "scale"
    )
    val scope = rememberCoroutineScope()

    val habitColor = remember(habit.color) {
        runCatching { Color(AndroidColor.parseColor(habit.color)) }.getOrDefault(Color(0xFF6366F1))
    }

    val cardBg = if (done) Color(0xFFF0FDF4) else Color.White
    val textDecoration = if (done) TextDecoration.LineThrough else TextDecoration.None
    val textColor = if (done) Color(0xFF16A34A) else Color(0xFF1E293B)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable {
                onToggle()
                if (!done) {
                    scope.launch {
                        animating = true
                        delay(150)
                        animating = false
                        delay(150)
                    }
                }
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(habitColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = habit.emoji, fontSize = 22.sp)
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = habit.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor,
                    textDecoration = textDecoration
                )
                Text(
                    text = habit.time,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            if (streak > 0) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0xFFFFF7ED)
                ) {
                    Text(
                        text = "🔥 $streak",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFEA580C),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Spacer(Modifier.width(8.dp))
            }

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(if (done) Emerald500 else Color.Transparent)
                    .then(
                        if (!done) Modifier.background(Color(0xFFE2E8F0), CircleShape)
                        else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (done) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Done",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
