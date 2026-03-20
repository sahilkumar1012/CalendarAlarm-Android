package com.sahil.calendaralarm.sync

import android.content.Context
import android.util.Log
import androidx.work.*
import com.sahil.calendaralarm.alarm.AlarmScheduler
import com.sahil.calendaralarm.data.CalendarRepository
import com.sahil.calendaralarm.data.PreferencesManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

class SyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "SyncWorker"
        const val WORK_NAME = "calendar_sync_daily"

        fun scheduleDailySync(context: Context, hour: Int, minute: Int) {
            val now = Calendar.getInstance()
            val target = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (before(now)) add(Calendar.DAY_OF_YEAR, 1)
            }

            val initialDelay = target.timeInMillis - now.timeInMillis

            val workRequest = PeriodicWorkRequestBuilder<SyncWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(false)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )

            Log.d(TAG, "Scheduled daily sync at $hour:${minute.toString().padStart(2, '0')}")
        }

        fun cancelDailySync(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            Log.d(TAG, "Cancelled daily sync")
        }
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Running sync worker")

        return try {
            val repository = CalendarRepository(applicationContext)
            val prefsManager = PreferencesManager(applicationContext)
            val scheduler = AlarmScheduler(applicationContext)

            val events = repository.getUpcomingEvents()
            val mutedIds = prefsManager.getMutedEventIds()
            val leadTime = prefsManager.getLeadTimeMinutes()

            val count = scheduler.scheduleAllAlarms(events, mutedIds, leadTime)
            prefsManager.setLastSyncTime(System.currentTimeMillis())

            Log.d(TAG, "Sync complete: ${events.size} events, $count alarms scheduled")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed: ${e.message}")
            Result.retry()
        }
    }
}
