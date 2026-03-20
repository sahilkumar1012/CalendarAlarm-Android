package com.sahil.calendaralarm.alarm

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.sahil.calendaralarm.R

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "AlarmReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val eventTitle = intent.getStringExtra(AlarmScheduler.EXTRA_EVENT_TITLE) ?: "Event"
        val eventDescription = intent.getStringExtra(AlarmScheduler.EXTRA_EVENT_DESCRIPTION) ?: ""
        val calendarName = intent.getStringExtra(AlarmScheduler.EXTRA_CALENDAR_NAME) ?: ""
        val eventId = intent.getLongExtra(AlarmScheduler.EXTRA_EVENT_ID, -1)

        Log.d(TAG, "Alarm triggered for: $eventTitle (id=$eventId)")

        // Build full-screen intent for AlarmActivity
        val alarmIntent = Intent(context, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(AlarmScheduler.EXTRA_EVENT_ID, eventId)
            putExtra(AlarmScheduler.EXTRA_EVENT_TITLE, eventTitle)
            putExtra(AlarmScheduler.EXTRA_EVENT_DESCRIPTION, eventDescription)
            putExtra(AlarmScheduler.EXTRA_CALENDAR_NAME, calendarName)
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            eventId.toInt(),
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Post a high-priority notification with fullScreenIntent.
        // - Screen OFF / locked → Android launches AlarmActivity immediately (real alarm)
        // - Screen ON → heads-up notification appears; tapping it opens AlarmActivity
        val notificationId = eventId.toInt() + AlarmScheduler.NOTIFICATION_ID_OFFSET
        val notification = NotificationCompat.Builder(context, "alarm_channel")
            .setSmallIcon(R.drawable.ic_alarm)
            .setContentTitle("⏰ Event Starting!")
            .setContentText(eventTitle)
            .setSubText(calendarName)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setAutoCancel(true)
            .setOngoing(true)  // can't swipe away — must dismiss via AlarmActivity
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
    }
}
