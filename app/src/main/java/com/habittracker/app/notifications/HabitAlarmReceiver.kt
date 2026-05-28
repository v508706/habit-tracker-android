package com.habittracker.app.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.habittracker.app.Constants
import com.habittracker.app.MainActivity
import com.habittracker.app.R

class HabitAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val habitId = intent.getStringExtra(Constants.EXTRA_HABIT_ID) ?: return
        val habitName = intent.getStringExtra(Constants.EXTRA_HABIT_NAME) ?: "Your habit"
        val habitEmoji = intent.getStringExtra(Constants.EXTRA_HABIT_EMOJI) ?: ""

        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, habitId.hashCode(), tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("$habitEmoji $habitName")
            .setContentText("Time for your $habitName! Tap to check it off.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val nm = context.getSystemService(NotificationManager::class.java)
        nm.notify(habitId.hashCode(), notification)
    }
}
