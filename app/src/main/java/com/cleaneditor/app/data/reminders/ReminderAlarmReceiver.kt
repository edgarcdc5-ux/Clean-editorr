package com.cleaneditor.app.data.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.cleaneditor.app.data.repository.ReminderRepository

class ReminderAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getLongExtra(EXTRA_REMINDER_ID, -1L)
        if (id < 0L) return

        val reminder = ReminderRepository(context).getAll().firstOrNull { it.id == id } ?: return
        if (reminder.completed || !reminder.daily || !reminder.alarmEnabled) return

        val serviceIntent = Intent(context, ReminderAlarmService::class.java).apply {
            putExtra(ReminderAlarmService.EXTRA_REMINDER_ID, reminder.id)
            putExtra(ReminderAlarmService.EXTRA_TITLE, reminder.title)
            putExtra(ReminderAlarmService.EXTRA_CONTENT, reminder.content)
        }
        ContextCompat.startForegroundService(context, serviceIntent)
        ReminderAlarmScheduler(context).schedule(reminder)
    }

    companion object {
        const val EXTRA_REMINDER_ID = "reminder_id"
    }
}
