package com.cleaneditor.app.data.reminders

import java.util.Calendar

/** Pure alarm-time validation and next-trigger calculation. */
object ReminderAlarmTime {
    fun parse(value: String): Pair<Int, Int>? {
        val parts = value.trim().split(":")
        if (parts.size != 2) return null
        val hour = parts[0].toIntOrNull() ?: return null
        val minute = parts[1].toIntOrNull() ?: return null
        if (hour !in 0..23 || minute !in 0..59) return null
        return hour to minute
    }

    fun nextTriggerMillis(alarmTime: String, nowMillis: Long = System.currentTimeMillis()): Long? {
        val (hour, minute) = parse(alarmTime) ?: return null
        val calendar = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= nowMillis) add(Calendar.DAY_OF_YEAR, 1)
        }
        return calendar.timeInMillis
    }
}
