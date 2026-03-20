package com.sahil.calendaralarm.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.sahil.calendaralarm.MainActivity
import com.sahil.calendaralarm.model.CalendarEvent

class AlarmScheduler(private val context: Context) {

    companion object {
        private const val TAG = "AlarmScheduler"
        const val EXTRA_EVENT_ID = "event_id"
        const val EXTRA_EVENT_TITLE = "event_title"
        const val EXTRA_EVENT_DESCRIPTION = "event_description"
        const val EXTRA_CALENDAR_NAME = "calendar_name"
        const val NOTIFICATION_ID_OFFSET = 20000
    }

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleAlarm(event: CalendarEvent, leadTimeMinutes: Int): Boolean {
        val triggerTime = event.startTime - (leadTimeMinutes * 60 * 1000L)

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

        // "Show" intent — shown when user taps alarm icon in status bar
        val showIntent = PendingIntent.getActivity(
            context,
            event.id.toInt(),
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // setAlarmClock is the strongest guarantee: never deferred by Doze,
        // shows alarm icon in status bar, exempt from all battery restrictions
        val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerTime, showIntent)
        try {
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            Log.d(TAG, "Scheduled alarm for '${event.title}' at $triggerTime")
            return true
        } catch (e: SecurityException) {
            Log.e(TAG, "Cannot schedule alarm clock, falling back: ${e.message}")
            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent
                )
                return true
            } catch (e2: SecurityException) {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent
                )
                return true
            }
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
            if (event.allDay) continue
            if (scheduleAlarm(event, leadTimeMinutes)) {
                count++
            }
        }
        Log.d(TAG, "Scheduled $count alarms out of ${events.size} events")
        return count
    }
}
