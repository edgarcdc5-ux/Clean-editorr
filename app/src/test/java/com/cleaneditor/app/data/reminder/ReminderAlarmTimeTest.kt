package com.cleaneditor.app.data.reminder

import com.cleaneditor.app.data.reminders.ReminderAlarmTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class ReminderAlarmTimeTest {
    @Test
    fun validTimeIsParsed() {
        assertEquals(8 to 5, ReminderAlarmTime.parse("08:05"))
        assertEquals(23 to 59, ReminderAlarmTime.parse("23:59"))
    }

    @Test
    fun invalidTimeIsRejected() {
        assertNull(ReminderAlarmTime.parse("24:00"))
        assertNull(ReminderAlarmTime.parse("12:60"))
        assertNull(ReminderAlarmTime.parse("1200"))
        assertNull(ReminderAlarmTime.parse(""))
    }

    @Test
    fun futureTimeSchedulesToday() {
        val now = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val trigger = ReminderAlarmTime.nextTriggerMillis("11:30", now.timeInMillis)!!
        val expected = Calendar.getInstance().apply {
            timeInMillis = now.timeInMillis
            set(Calendar.HOUR_OF_DAY, 11)
            set(Calendar.MINUTE, 30)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        assertEquals(expected.timeInMillis, trigger)
    }

    @Test
    fun currentOrPastTimeSchedulesNextDay() {
        val now = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 18)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val trigger = ReminderAlarmTime.nextTriggerMillis("17:00", now.timeInMillis)!!
        val expected = Calendar.getInstance().apply {
            timeInMillis = now.timeInMillis
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 17)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        assertEquals(expected.timeInMillis, trigger)
        assertTrue(trigger > now.timeInMillis)
    }
}
