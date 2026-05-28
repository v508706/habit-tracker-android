package com.habittracker.app.domain.model

data class UserProfile(
    val name: String,
    val setupDone: Boolean,
    val timezone: String,
    val fcmToken: String? = null
)
