package com.cassinopros.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object SessionNotificationHelper {
    private const val CHANNEL_ID = "session_limits"
    private const val CHANNEL_NAME = "Limites da sessão"

    fun notifyLimit(context: Context, sessionId: Long, title: String, resultCents: Long) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Alertas quando uma sessão atinge Meta ou Stop Loss."
                }
            )
        }

        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(context, CHANNEL_ID)
        } else {
            Notification.Builder(context)
        }
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText("Resultado da sessão: ${money(resultCents)}")
            .setAutoCancel(true)
            .setCategory(Notification.CATEGORY_STATUS)
            .build()

        manager.notify((sessionId % Int.MAX_VALUE).toInt(), notification)
    }

    private fun money(cents: Long): String =
        String.format(java.util.Locale("pt", "BR"), "R$ %,.2f", cents / 100.0)
}
