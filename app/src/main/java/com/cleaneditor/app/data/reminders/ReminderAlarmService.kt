package com.cleaneditor.app.data.reminders

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.cleaneditor.app.R

class ReminderAlarmService : Service() {
    private var mediaPlayer: MediaPlayer? = null
    private var reminderId: Long = -1L

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopAlarm()
            return START_NOT_STICKY
        }

        reminderId = intent?.getLongExtra(EXTRA_REMINDER_ID, -1L) ?: -1L
        val title = intent?.getStringExtra(EXTRA_TITLE).orEmpty().ifBlank { "Lembrete" }
        val content = intent?.getStringExtra(EXTRA_CONTENT).orEmpty().ifBlank { "Seu lembrete está aguardando." }

        createChannel()
        startForeground(NOTIFICATION_ID, buildNotification(title, content))
        startSound()
        return START_NOT_STICKY
    }

    private fun startSound() {
        if (mediaPlayer?.isPlaying == true) return
        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            ?: return

        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            setDataSource(this@ReminderAlarmService, uri)
            isLooping = true
            prepare()
            start()
        }
    }

    private fun buildNotification(title: String, content: String): Notification {
        val stopIntent = PendingIntent.getService(
            this,
            reminderId.hashCode(),
            Intent(this, ReminderAlarmService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val fullScreenIntent = PendingIntent.getActivity(
            this,
            reminderId.hashCode() xor 0x5A5A5A5A,
            Intent(this, ReminderAlarmActivity::class.java).apply {
                putExtra(EXTRA_REMINDER_ID, reminderId)
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_CONTENT, content)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true)
            .setAutoCancel(false)
            .setFullScreenIntent(fullScreenIntent, true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Desativar", stopIntent)
            .build()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "Alarmes dos lembretes",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Som contínuo dos lembretes do CleanEditor até serem desativados"
                    setSound(null, null)
                    enableVibration(true)
                }
            )
        }
    }

    private fun stopAlarm() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        mediaPlayer?.release()
        mediaPlayer = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_STOP = "com.cleaneditor.app.action.STOP_REMINDER_ALARM"
        const val EXTRA_REMINDER_ID = "reminder_id"
        const val EXTRA_TITLE = "reminder_title"
        const val EXTRA_CONTENT = "reminder_content"
        private const val CHANNEL_ID = "reminder_alarm"
        private const val NOTIFICATION_ID = 9001

        fun stop(context: Context) {
            context.startService(Intent(context, ReminderAlarmService::class.java).setAction(ACTION_STOP))
        }
    }
}
