package com.cleaneditor.app.data.reminders

import java.time.LocalDate
import java.time.LocalTime

/** Domain model for a reminder created from editor/AI content. */
data class Reminder(
    val id: Long,
    val title: String,
    val description: String = "",
    val date: LocalDate? = null,
    val time: LocalTime? = null,
    val category: String = "Geral",
    val priority: ReminderPriority = ReminderPriority.MEDIUM,
    val completed: Boolean = false
)

enum class ReminderPriority {
    LOW,
    MEDIUM,
    HIGH
}
