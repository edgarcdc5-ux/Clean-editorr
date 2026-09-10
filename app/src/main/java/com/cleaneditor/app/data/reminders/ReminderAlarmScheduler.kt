package com.cleaneditor.app.data.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.cleaneditor.app.data.model.Reminder
import java.util.Calendar

class ReminderAlarmScheduler(context: Context) {
    private val appContext = context.applicationContext
    private val alarmManager = appContext.getSystemService(AlarmManager::class.java)

    fun schedule(reminder: Reminder) {
        if (!reminder.daily || !reminder.alarmEnabled || reminder.completed || reminder.alarmTime.isBlank()) return
        val parts = reminder.alarmTime.split(":")
        if (parts.size != 2) return
        val hour = parts[0].toIntOrNull() ?: return
        val minute = parts[1].toIntOrNull() ?: return
        if (hour !in 0..23 || minute !in 0..59) return

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }
        val operation = pendingIntent(reminder.id)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, operation)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, operation)
        }
    }

    fun cancel(reminderId: Long) {
        alarmManager.cancel(pendingIntent(reminderId))
    }

    private fun pendingIntent(id: Long): PendingIntent = PendingIntent.getBroadcast(
        appContext, id.hashCode(),
        Intent(appContext, ReminderAlarmReceiver::class.java).putExtra(ReminderAlarmReceiver.EXTRA_REMINDER_ID, id),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}
