package com.habittracker.app.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.habittracker.app.Constants
import com.habittracker.app.domain.model.Habit
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val timeFmt = DateTimeFormatter.ofPattern("HH:mm")

    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_ID,
                context.getString(com.habittracker.app.R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for your daily habits"
                enableVibration(true)
            }
            val nm = context.getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }

    fun scheduleAll(habits: List<Habit>) {
        cancelAll(habits.map { it.id })
        habits.forEach { scheduleHabit(it) }
    }

    private fun scheduleHabit(habit: Habit) {
        val parts = habit.time.split(":").map { it.toIntOrNull() ?: 0 }
        val hour = parts.getOrElse(0) { 8 }
        val minute = parts.getOrElse(1) { 0 }
        val habitTime = LocalTime.of(hour, minute)
        val today = LocalDate.now()

        // Schedule for next 14 days
        for (i in 0 until 14) {
            val date = today.plusDays(i.toLong())
            val dayName = com.habittracker.app.Constants.DAY_NAMES[date.dayOfWeek.value - 1]
            if (dayName in habit.days) {
                val triggerTime = LocalDateTime.of(date, habitTime)
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()

                if (triggerTime > System.currentTimeMillis()) {
                    scheduleAlarm(habit, triggerTime, date.toString(), i)
                }
            }
        }
    }

    private fun scheduleAlarm(habit: Habit, triggerMs: Long, date: String, dayOffset: Int) {
        val requestCode = "${habit.id}_$date".hashCode()
        val intent = Intent(context, HabitAlarmReceiver::class.java).apply {
            putExtra(Constants.EXTRA_HABIT_ID, habit.id)
            putExtra(Constants.EXTRA_HABIT_NAME, habit.name)
            putExtra(Constants.EXTRA_HABIT_EMOJI, habit.emoji)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMs, pendingIntent)
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, triggerMs, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMs, pendingIntent)
            }
        } catch (e: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerMs, pendingIntent)
        }
    }

    fun cancelAll(habitIds: List<String>) {
        val today = LocalDate.now()
        habitIds.forEach { habitId ->
            for (i in 0 until 14) {
                val date = today.plusDays(i.toLong())
                val requestCode = "${habitId}_$date".hashCode()
                val intent = Intent(context, HabitAlarmReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(
                    context, requestCode, intent,
                    PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
                )
                pendingIntent?.let { alarmManager.cancel(it) }
            }
        }
    }
}
