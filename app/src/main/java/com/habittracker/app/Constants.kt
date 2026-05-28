package com.habittracker.app

object Constants {
    val HABIT_COLORS = listOf(
        "#ef4444", "#f97316", "#eab308", "#22c55e",
        "#14b8a6", "#3b82f6", "#6366f1", "#a855f7",
        "#ec4899", "#64748b"
    )

    val DAY_NAMES = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    val HABIT_EMOJIS = listOf(
        "🏃", "🏃‍♂️", "🚶", "🧘", "🏋️", "🏊", "🚴", "💪",
        "♟️", "♜", "🏰",
        "🎵", "🎸", "🎹", "⌨️", "🎤",
        "📚", "📖", "📗", "📕", "✏️", "📝", "🧠", "🔢", "📰",
        "🗣️", "🎓", "🏫",
        "💊", "💧", "🛌", "🦷", "🍎", "🥗", "☕",
        "🎨", "🖍️", "✍️", "📷",
        "🤖", "🧮", "🧩", "🌱",
        "🛕", "🌠", "🎯", "🐾", "🧹", "🎃"
    )

    const val NOTIFICATION_CHANNEL_ID = "habit_reminders"
    const val EXTRA_HABIT_ID = "habit_id"
    const val EXTRA_HABIT_NAME = "habit_name"
    const val EXTRA_HABIT_EMOJI = "habit_emoji"
}
