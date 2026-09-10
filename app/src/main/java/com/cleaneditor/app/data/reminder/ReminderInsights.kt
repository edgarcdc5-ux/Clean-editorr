package com.cleaneditor.app.data.reminder

import com.cleaneditor.app.data.model.Reminder

/** Lightweight derived reminder statistics; keeps UI work O(n) and free of persistence side effects. */
data class ReminderInsights(
    val total: Int,
    val pending: Int,
    val completed: Int,
    val byPriority: Map<String, Int>,
    val byCategory: Map<String, Int>
)

object ReminderInsightsCalculator {
    fun calculate(reminders: List<Reminder>): ReminderInsights {
        val pending = reminders.count { !it.completed }
        return ReminderInsights(
            total = reminders.size,
            pending = pending,
            completed = reminders.size - pending,
            byPriority = reminders.groupingBy { it.priority.ifBlank { "Média" } }.eachCount(),
            byCategory = reminders.groupingBy { it.category.ifBlank { "Geral" } }.eachCount()
        )
    }
}
