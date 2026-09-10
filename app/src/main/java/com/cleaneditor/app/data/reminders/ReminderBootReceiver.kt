package com.cleaneditor.app.data.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.cleaneditor.app.data.repository.ReminderRepository

class ReminderBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED && intent.action != Intent.ACTION_MY_PACKAGE_REPLACED) return
        val scheduler = ReminderAlarmScheduler(context)
        ReminderRepository(context).getAll().forEach { scheduler.schedule(it) }
    }
}
