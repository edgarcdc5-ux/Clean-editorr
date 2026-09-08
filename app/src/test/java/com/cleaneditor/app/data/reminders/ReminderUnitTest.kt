package com.cleaneditor.app.data.reminders

import java.time.LocalDate
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReminderUnitTest {
    @Test
    fun `default reminder has expected optional values and medium priority`() {
        val reminder = Reminder(id = 1L, title = "Comprar leite")

        assertEquals("Comprar leite", reminder.title)
        assertEquals("", reminder.description)
        assertEquals("Geral", reminder.category)
        assertEquals(ReminderPriority.MEDIUM, reminder.priority)
        assertFalse(reminder.completed)
        assertEquals(null, reminder.date)
        assertEquals(null, reminder.time)
    }

    @Test
    fun `reminder preserves date time category priority and completion`() {
        val reminder = Reminder(
            id = 7L,
            title = "Reunião",
            description = "Levar pauta",
            date = LocalDate.of(2026, 9, 15),
            time = LocalTime.of(14, 30),
            category = "Trabalho",
            priority = ReminderPriority.HIGH,
            completed = true
        )

        assertEquals(7L, reminder.id)
        assertEquals(LocalDate.of(2026, 9, 15), reminder.date)
        assertEquals(LocalTime.of(14, 30), reminder.time)
        assertEquals("Trabalho", reminder.category)
        assertEquals(ReminderPriority.HIGH, reminder.priority)
        assertTrue(reminder.completed)
    }

    @Test
    fun `priority enum exposes low medium and high`() {
        assertEquals(
            listOf(ReminderPriority.LOW, ReminderPriority.MEDIUM, ReminderPriority.HIGH),
            ReminderPriority.entries
        )
    }
}
