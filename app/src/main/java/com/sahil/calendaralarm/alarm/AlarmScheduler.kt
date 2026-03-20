package com.sahil.calendaralarm.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.sahil.calendaralarm.model.CalendarEvent

class AlarmScheduler(private val context: Context) {

    companion object {
        private const val TAG = "AlarmScheduler"
        const val EXTRA_EVENT_ID = "event_id"
        const val EXTRA_EVENT_TITLE = "event_title"
        const val EXTRA_EVENT_DESCRIPTION = "event_description"
        const val EXTRA_CALENDAR_NAME = "calendar_name"
    }

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleAlarm(event: CalendarEvent, leadTimeMinutes: Int): Boolean {
        val triggerTime = event.startTime - (leadTimeMinutes * 60 * 1000L)

        // Don't schedule alarms for past events
        if (triggerTime <= System.currentTimeMillis()) {
            return false
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_EVENT_ID, event.id)
            putExtra(EXTRA_EVENT_TITLE, event.title)
            putExtra(EXTRA_EVENT_DESCRIPTION, event.description)
            putExtra(EXTRA_CALENDAR_NAME, event.calendarName)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            event.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent
                    )
                } else {
                    // Fallback to inexact alarm
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent
                )
            }
            Log.d(TAG, "Scheduled alarm for '${event.title}' at $triggerTime")
            return true
        } catch (e: SecurityException) {
            Log.e(TAG, "Cannot schedule exact alarm: ${e.message}")
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent
            )
            return true
        }
    }

    fun cancelAlarm(eventId: Long) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            eventId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        Log.d(TAG, "Cancelled alarm for event $eventId")
    }

    fun scheduleAllAlarms(
        events: List<CalendarEvent>,
        mutedEventIds: Set<String>,
        leadTimeMinutes: Int
    ): Int {
        var count = 0
        for (event in events) {
            if (event.id.toString() in mutedEventIds) continue
            if (event.allDay) continue // skip all-day events
            if (scheduleAlarm(event, leadTimeMinutes)) {
                count++
            }
        }
        Log.d(TAG, "Scheduled $count alarms out of ${events.size} events")
        return count
    }
}
