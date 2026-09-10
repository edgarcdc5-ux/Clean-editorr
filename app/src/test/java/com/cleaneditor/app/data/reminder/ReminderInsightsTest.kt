package com.cleaneditor.app.data.reminder

import com.cleaneditor.app.data.model.Reminder
import org.junit.Assert.assertEquals
import org.junit.Test

class ReminderInsightsTest {
    @Test
    fun calculatesTotalsAndGroups() {
        val items = listOf(
            reminder(1, "Alta", "Trabalho", false),
            reminder(2, "Alta", "Trabalho", true),
            reminder(3, "Baixa", "Pessoal", false)
        )

        val result = ReminderInsightsCalculator.calculate(items)

        assertEquals(3, result.total)
        assertEquals(2, result.pending)
        assertEquals(1, result.completed)
        assertEquals(2, result.byPriority["Alta"])
        assertEquals(2, result.byCategory["Trabalho"])
    }

    @Test
    fun blankPriorityAndCategoryUseSafeDefaults() {
        val result = ReminderInsightsCalculator.calculate(listOf(reminder(1, "", "", false)))
        assertEquals(1, result.byPriority["Média"])
        assertEquals(1, result.byCategory["Geral"])
    }

    private fun reminder(id: Long, priority: String, category: String, completed: Boolean) = Reminder(
        id = id,
        title = "Teste",
        content = "Conteúdo",
        date = "",
        priority = priority,
        category = category,
        completed = completed
    )
}
