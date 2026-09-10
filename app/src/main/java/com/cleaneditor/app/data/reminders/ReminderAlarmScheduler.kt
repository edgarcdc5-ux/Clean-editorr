package com.cleaneditor.app.data.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.cleaneditor.app.data.model.Reminder

class ReminderAlarmScheduler(context: Context) {
    private val appContext = context.applicationContext
    private val alarmManager = appContext.getSystemService(AlarmManager::class.java)

    fun schedule(reminder: Reminder) {
        // Always replace an existing alarm for this reminder. This prevents an old
        // time from surviving when the user edits the reminder time/settings.
        cancel(reminder.id)
        if (!reminder.daily || !reminder.alarmEnabled || reminder.completed || reminder.alarmTime.isBlank()) return

        val triggerAtMillis = ReminderAlarmTime.nextTriggerMillis(reminder.alarmTime) ?: return
        val operation = pendingIntent(reminder.id)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation)
        }
    }

    fun cancel(reminderId: Long) {
        alarmManager.cancel(pendingIntent(reminderId))
    }

    private fun pendingIntent(id: Long): PendingIntent = PendingIntent.getBroadcast(
        appContext,
        id.hashCode(),
        Intent(appContext, ReminderAlarmReceiver::class.java)
            .putExtra(ReminderAlarmReceiver.EXTRA_REMINDER_ID, id),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}
