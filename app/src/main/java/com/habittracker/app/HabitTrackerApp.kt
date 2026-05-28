package com.habittracker.app

import android.app.Application
import com.habittracker.app.notifications.NotificationScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class HabitTrackerApp : Application() {

    @Inject lateinit var notificationScheduler: NotificationScheduler

    override fun onCreate() {
        super.onCreate()
        notificationScheduler.createNotificationChannel()
    }
}
