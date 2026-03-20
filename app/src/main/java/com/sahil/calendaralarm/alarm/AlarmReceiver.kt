package com.sahil.calendaralarm.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

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

        // Launch full-screen alarm activity
        val alarmIntent = Intent(context, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(AlarmScheduler.EXTRA_EVENT_ID, eventId)
            putExtra(AlarmScheduler.EXTRA_EVENT_TITLE, eventTitle)
            putExtra(AlarmScheduler.EXTRA_EVENT_DESCRIPTION, eventDescription)
            putExtra(AlarmScheduler.EXTRA_CALENDAR_NAME, calendarName)
        }
        context.startActivity(alarmIntent)
    }
}
