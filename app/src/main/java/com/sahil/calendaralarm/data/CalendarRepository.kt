package com.sahil.calendaralarm.data

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.provider.CalendarContract
import com.sahil.calendaralarm.model.CalendarEvent
import java.util.Calendar

class CalendarRepository(private val context: Context) {

    fun getUpcomingEvents(daysAhead: Int = 7): List<CalendarEvent> {
        val events = mutableListOf<CalendarEvent>()
        val now = System.currentTimeMillis()
        val endRange = now + daysAhead * 24L * 60 * 60 * 1000

        val projection = arrayOf(
            CalendarContract.Instances.EVENT_ID,
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.DESCRIPTION,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END,
            CalendarContract.Instances.CALENDAR_DISPLAY_NAME,
            CalendarContract.Instances.CALENDAR_COLOR,
            CalendarContract.Instances.ALL_DAY
        )

        val selection = "${CalendarContract.Instances.BEGIN} >= ? AND ${CalendarContract.Instances.BEGIN} <= ?"
        val selectionArgs = arrayOf(now.toString(), endRange.toString())
        val sortOrder = "${CalendarContract.Instances.BEGIN} ASC"

        val uri = CalendarContract.Instances.CONTENT_URI.buildUpon()
            .appendPath(now.toString())
            .appendPath(endRange.toString())
            .build()

        val cursor: Cursor? = context.contentResolver.query(
            uri, projection, null, null, sortOrder
        )

        cursor?.use {
            while (it.moveToNext()) {
                val event = CalendarEvent(
                    id = it.getLong(0),
                    title = it.getString(1) ?: "(No title)",
                    description = it.getString(2) ?: "",
                    startTime = it.getLong(3),
                    endTime = it.getLong(4),
                    calendarName = it.getString(5) ?: "",
                    calendarColor = it.getInt(6),
                    allDay = it.getInt(7) == 1
                )
                events.add(event)
            }
        }

        return events
    }
}
