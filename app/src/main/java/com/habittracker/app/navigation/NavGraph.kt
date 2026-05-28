package com.habittracker.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.google.firebase.auth.FirebaseAuth
import com.habittracker.app.ui.auth.LoginScreen
import com.habittracker.app.ui.dashboard.DashboardScreen
import com.habittracker.app.ui.habits.HabitManagerScreen
import com.habittracker.app.ui.setup.SetupWizardScreen
import com.habittracker.app.ui.stats.HabitDetailScreen
import com.habittracker.app.ui.stats.StatsScreen
import com.habittracker.app.ui.theme.Indigo600

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Setup : Screen("setup")
    object Main : Screen("main")
    object HabitDetail : Screen("habit_detail/{habitId}") {
        fun createRoute(habitId: String) = "habit_detail/$habitId"
    }
}

sealed class BottomTab(val route: String, val label: String, val emoji: String) {
    object Today : BottomTab("today", "Today", "📅")
    object Habits : BottomTab("habits", "Habits", "✨")
    object Stats : BottomTab("stats", "Stats", "📊")
}

@Composable
fun AppNavGraph(auth: FirebaseAuth) {
    val navController = rememberNavController()
    val startDest = if (auth.currentUser != null) Screen.Main.route else Screen.Login.route

    NavHost(navController = navController, startDestination = startDest) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { setupDone ->
                    val dest = if (setupDone) Screen.Main.route else Screen.Setup.route
                    navController.navigate(dest) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Setup.route) {
            SetupWizardScreen(
                onSetupComplete = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Setup.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Main.route) {
            MainScreen(
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onHabitDetail = { habitId ->
                    navController.navigate(Screen.HabitDetail.createRoute(habitId))
                }
            )
        }

        composable(Screen.HabitDetail.route) {
            HabitDetailScreen(onBack = { navController.popBackStack() })
        }
    }
}

@Composable
private fun MainScreen(onLogout: () -> Unit, onHabitDetail: (String) -> Unit) {
    val tabs = listOf(BottomTab.Today, BottomTab.Habits, BottomTab.Stats)
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDest = navBackStackEntry?.destination

                tabs.forEach { tab ->
                    val selected = currentDest?.hierarchy?.any { it.route == tab.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Text(tab.emoji) },
                        label = { Text(tab.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Indigo600,
                            selectedTextColor = Indigo600,
                            indicatorColor = Indigo600.copy(alpha = 0.1f)
                        )
                    )
                }

                // Logout tab
                NavigationBarItem(
                    selected = false,
                    onClick = {
                        com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                        onLogout()
                    },
                    icon = { Text("🚪") },
                    label = { Text("Logout") },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedTextColor = Color.Gray,
                        unselectedIconColor = Color.Gray
                    )
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = BottomTab.Today.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(BottomTab.Today.route) { DashboardScreen() }
            composable(BottomTab.Habits.route) { HabitManagerScreen() }
            composable(BottomTab.Stats.route) {
                StatsScreen(onHabitClick = { onHabitDetail(it) })
            }
        }
    }
}
