package com.sahil.calendaralarm.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.sahil.calendaralarm.data.CalendarRepository
import com.sahil.calendaralarm.data.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_LOCKED_BOOT_COMPLETED
        ) {
            Log.d(TAG, "Device booted — re-scheduling alarms")

            val pendingResult = goAsync()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val repository = CalendarRepository(context)
                    val prefsManager = PreferencesManager(context)
                    val scheduler = AlarmScheduler(context)

                    val events = repository.getUpcomingEvents()
                    val mutedIds = prefsManager.getMutedEventIds()
                    val leadTime = prefsManager.getLeadTimeMinutes()

                    val count = scheduler.scheduleAllAlarms(events, mutedIds, leadTime)
                    Log.d(TAG, "Re-scheduled $count alarms after boot")
                } catch (e: Exception) {
                    Log.e(TAG, "Error re-scheduling alarms: ${e.message}")
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
