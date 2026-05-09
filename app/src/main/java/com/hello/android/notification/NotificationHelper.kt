package com.hello.android.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.hello.android.MainActivity
import com.hello.android.R

object NotificationHelper {

    private const val CHANNEL_ID_COUNTER = "counter_notifications"
    private const val CHANNEL_NAME_COUNTER = "Counter Notifications"
    private const val CHANNEL_DESCRIPTION_COUNTER = "Notifications for counter milestones"

    private const val CHANNEL_ID_POSTS = "posts_notifications"
    private const val CHANNEL_NAME_POSTS = "New Content"
    private const val CHANNEL_DESCRIPTION_POSTS = "Notifications for new posts and content updates"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Counter notifications channel
            val counterChannel = NotificationChannel(
                CHANNEL_ID_COUNTER,
                CHANNEL_NAME_COUNTER,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION_COUNTER
            }
            notificationManager.createNotificationChannel(counterChannel)

            // Posts notifications channel
            val postsChannel = NotificationChannel(
                CHANNEL_ID_POSTS,
                CHANNEL_NAME_POSTS,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION_POSTS
            }
            notificationManager.createNotificationChannel(postsChannel)
        }
    }

    fun showCounterNotification(context: Context, count: Int) {
        // Create intent to open app when notification is clicked
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_COUNTER)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notification_counter_title))
            .setContentText(context.getString(R.string.notification_counter_text, count))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(count, notification)
        } catch (e: SecurityException) {
            // Notification permission not granted
        }
    }

    fun showNewPostsNotification(context: Context, count: Int) {
        // Create intent to open app when notification is clicked
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (count > 0) {
            context.getString(R.string.notification_new_posts_title)
        } else {
            context.getString(R.string.notification_posts_refresh_title)
        }
        val text = if (count > 0) {
            context.getString(R.string.notification_new_posts_text, count)
        } else {
            context.getString(R.string.notification_posts_refresh_text)
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_POSTS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_POSTS, notification)
        } catch (e: SecurityException) {
            // Notification permission not granted
        }
    }

    private const val NOTIFICATION_ID_POSTS = 1001
}
