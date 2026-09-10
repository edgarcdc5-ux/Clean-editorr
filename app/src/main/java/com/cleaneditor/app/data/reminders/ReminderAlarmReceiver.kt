package com.cleaneditor.app.data.reminders

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.cleaneditor.app.MainActivity
import com.cleaneditor.app.data.model.Reminder
import com.cleaneditor.app.data.repository.ReminderRepository

class ReminderAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getLongExtra(EXTRA_REMINDER_ID, -1L)
        if (id < 0L) return
        val reminder = ReminderRepository(context).getAll().firstOrNull { it.id == id } ?: return
        if (reminder.completed || !reminder.daily || !reminder.alarmEnabled) return

        val manager = context.getSystemService(NotificationManager::class.java)
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Lembretes", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Alarmes dos lembretes diários do CleanEditor"
                }
            )
        }
        val openIntent = PendingIntent.getActivity(
            context, id.hashCode(), Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(reminder.title.ifBlank { "Lembrete" })
            .setContentText(reminder.content.ifBlank { "Seu lembrete diário está aguardando." })
            .setStyle(NotificationCompat.BigTextStyle().bigText(reminder.content.ifBlank { "Seu lembrete diário está aguardando." }))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openIntent)
            .build()
        manager.notify(id.hashCode(), notification)
        ReminderAlarmScheduler(context).schedule(reminder)
    }

    companion object {
        const val EXTRA_REMINDER_ID = "reminder_id"
        private const val CHANNEL_ID = "daily_reminders"
    }
}
