package com.habittracker.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.google.firebase.auth.FirebaseAuth
import com.habittracker.app.data.repository.HabitRepository
import com.habittracker.app.navigation.AppNavGraph
import com.habittracker.app.notifications.NotificationScheduler
import com.habittracker.app.ui.theme.HabitTrackerTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var auth: FirebaseAuth
    @Inject lateinit var repository: HabitRepository
    @Inject lateinit var scheduler: NotificationScheduler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Reschedule notifications on app start
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                val habits = repository.getHabits()
                scheduler.scheduleAll(habits)
            }
        }

        setContent {
            HabitTrackerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavGraph(auth = auth)
                }
            }
        }
    }
}
