package com.sahil.calendaralarm

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.sahil.calendaralarm.data.PreferencesManager
import com.sahil.calendaralarm.sync.SyncWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CalendarAlarmApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        initAutoSync()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "alarm_channel",
            getString(R.string.alarm_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = getString(R.string.alarm_channel_description)
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 1000, 500, 1000)
            setBypassDnd(true)
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun initAutoSync() {
        CoroutineScope(Dispatchers.IO).launch {
            val prefs = PreferencesManager(this@CalendarAlarmApplication)
            if (prefs.isAutoSyncEnabled()) {
                val hour = prefs.getSyncHour()
                val minute = prefs.getSyncMinute()
                SyncWorker.scheduleDailySync(this@CalendarAlarmApplication, hour, minute)
            }
        }
    }
}
