package com.sahil.calendaralarm.model

data class CalendarEvent(
    val id: Long,
    val title: String,
    val description: String,
    val startTime: Long,   // epoch millis
    val endTime: Long,     // epoch millis
    val calendarName: String,
    val calendarColor: Int,
    val allDay: Boolean
)
