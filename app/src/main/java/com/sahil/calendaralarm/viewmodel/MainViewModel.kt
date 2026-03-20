package com.sahil.calendaralarm.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sahil.calendaralarm.alarm.AlarmScheduler
import com.sahil.calendaralarm.data.CalendarRepository
import com.sahil.calendaralarm.data.PreferencesManager
import com.sahil.calendaralarm.model.CalendarEvent
import com.sahil.calendaralarm.sync.SyncWorker
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class MainUiState(
    val events: List<CalendarEvent> = emptyList(),
    val mutedEventIds: Set<String> = emptySet(),
    val isSyncing: Boolean = false,
    val lastSyncTime: Long = 0L,
    val lastSyncResult: String? = null,
    val autoSyncEnabled: Boolean = true,
    val syncHour: Int = 6,
    val syncMinute: Int = 0,
    val leadTimeMinutes: Int = 1,
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "MainViewModel"
    }

    private val calendarRepository = CalendarRepository(application)
    private val preferencesManager = PreferencesManager(application)
    private val alarmScheduler = AlarmScheduler(application)

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        // Collect preferences
        viewModelScope.launch {
            combine(
                preferencesManager.autoSyncEnabled,
                preferencesManager.syncHour,
                preferencesManager.syncMinute,
                preferencesManager.leadTimeMinutes,
                preferencesManager.lastSyncTime,
                preferencesManager.mutedEventIds,
            ) { values ->
                _uiState.update { state ->
                    state.copy(
                        autoSyncEnabled = values[0] as Boolean,
                        syncHour = values[1] as Int,
                        syncMinute = values[2] as Int,
                        leadTimeMinutes = values[3] as Int,
                        lastSyncTime = values[4] as Long,
                        mutedEventIds = (values[5] as Set<*>).filterIsInstance<String>().toSet(),
                    )
                }
            }.collect()
        }
    }

    fun syncEvents() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true, lastSyncResult = null) }

            try {
                val events = calendarRepository.getUpcomingEvents()
                val mutedIds = preferencesManager.getMutedEventIds()
                val leadTime = preferencesManager.getLeadTimeMinutes()

                val alarmsScheduled = alarmScheduler.scheduleAllAlarms(events, mutedIds, leadTime)
                preferencesManager.setLastSyncTime(System.currentTimeMillis())

                _uiState.update {
                    it.copy(
                        events = events,
                        isSyncing = false,
                        lastSyncResult = "${events.size} events synced, $alarmsScheduled alarms set"
                    )
                }

                Log.d(TAG, "Sync complete: ${events.size} events, $alarmsScheduled alarms")
            } catch (e: Exception) {
                Log.e(TAG, "Sync failed: ${e.message}")
                _uiState.update {
                    it.copy(
                        isSyncing = false,
                        lastSyncResult = "Sync failed: ${e.message}"
                    )
                }
            }
        }
    }

    fun toggleEventMute(eventId: String) {
        viewModelScope.launch {
            preferencesManager.toggleMutedEvent(eventId)
            // Re-sync to update alarms
            syncEvents()
        }
    }

    fun setAutoSync(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setAutoSyncEnabled(enabled)
            if (enabled) {
                val hour = preferencesManager.getSyncHour()
                val minute = preferencesManager.getSyncMinute()
                SyncWorker.scheduleDailySync(getApplication(), hour, minute)
            } else {
                SyncWorker.cancelDailySync(getApplication())
            }
        }
    }

    fun setSyncTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            preferencesManager.setSyncTime(hour, minute)
            if (preferencesManager.isAutoSyncEnabled()) {
                SyncWorker.scheduleDailySync(getApplication(), hour, minute)
            }
        }
    }

    fun setLeadTime(minutes: Int) {
        viewModelScope.launch {
            preferencesManager.setLeadTimeMinutes(minutes)
            syncEvents() // re-schedule with new lead time
        }
    }

    fun clearSyncResult() {
        _uiState.update { it.copy(lastSyncResult = null) }
    }
}
