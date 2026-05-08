package com.example.shabasher.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.shabasher.MainActivity
import com.example.shabasher.Model.AppNotification
import com.example.shabasher.Model.NotificationCategory
import com.example.shabasher.R

/**
 * Обёртка над Android NotificationManager.
 * Показывает уведомления в системной шторке, создаёт каналы для Android 8+.
 */
class SystemNotifier(private val context: Context) {

    private val manager: NotificationManagerCompat = NotificationManagerCompat.from(context)

    fun ensureChannelsCreated() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val fundChannel = NotificationChannel(
            CHANNEL_FUNDRAISE,
            "Сборы",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Уведомления об оплатах, подтверждениях и закрытии сборов"
            enableVibration(true)
        }

        val eventChannel = NotificationChannel(
            CHANNEL_EVENT,
            "События",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Изменения и приглашения в события"
        }

        val sysChannel = NotificationChannel(
            CHANNEL_SYSTEM,
            "Системные",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Прочие сообщения приложения"
        }

        nm.createNotificationChannels(listOf(fundChannel, eventChannel, sysChannel))
    }

    fun show(notification: AppNotification) {
        // На Android 13+ нужно явное разрешение POST_NOTIFICATIONS — без него show просто no-op.
        if (!hasPermission()) return

        val channelId = when (notification.category) {
            NotificationCategory.Fundraise -> CHANNEL_FUNDRAISE
            NotificationCategory.Event -> CHANNEL_EVENT
            NotificationCategory.System -> CHANNEL_SYSTEM
        }

        val pendingIntent = buildContentIntent(notification)

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(notification.title)
            .setContentText(notification.body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notification.body))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(
                when (notification.category) {
                    NotificationCategory.Fundraise -> NotificationCompat.PRIORITY_HIGH
                    NotificationCategory.Event -> NotificationCompat.PRIORITY_DEFAULT
                    NotificationCategory.System -> NotificationCompat.PRIORITY_LOW
                }
            )

        try {
            manager.notify(notification.id.hashCode(), builder.build())
        } catch (_: SecurityException) {
            // нет разрешения — игнорируем
        }
    }

    fun cancel(notificationId: String) {
        manager.cancel(notificationId.hashCode())
    }

    fun cancelAll() {
        manager.cancelAll()
    }

    private fun hasPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    /**
     * Клик по системному уведомлению открывает MainActivity и через deep link
     * (схема `shabasher-app://notification?route=...`) пересылает на нужный экран.
     */
    private fun buildContentIntent(notification: AppNotification): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            // Передаём целевой маршрут как extra
            notification.targetRoute?.let { putExtra(EXTRA_TARGET_ROUTE, it) }
            putExtra(EXTRA_NOTIFICATION_ID, notification.id)
            // Уникальный data, чтобы PendingIntent не реюзался между уведомлениями
            data = Uri.parse("shabasher-notification://${notification.id}")
        }
        return PendingIntent.getActivity(
            context,
            notification.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val CHANNEL_FUNDRAISE = "fundraise"
        const val CHANNEL_EVENT = "event"
        const val CHANNEL_SYSTEM = "system"

        const val EXTRA_TARGET_ROUTE = "notification_target_route"
        const val EXTRA_NOTIFICATION_ID = "notification_id"
    }
}
