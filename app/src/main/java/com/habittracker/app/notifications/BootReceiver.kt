package com.habittracker.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.habittracker.app.data.repository.HabitRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var repository: HabitRepository
    @Inject lateinit var scheduler: NotificationScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val habits = repository.getHabits()
                scheduler.scheduleAll(habits)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
