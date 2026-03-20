package com.sahil.calendaralarm.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesManager(private val context: Context) {

    companion object {
        private val AUTO_SYNC_ENABLED = booleanPreferencesKey("auto_sync_enabled")
        private val SYNC_HOUR = intPreferencesKey("sync_hour")
        private val SYNC_MINUTE = intPreferencesKey("sync_minute")
        private val LEAD_TIME_MINUTES = intPreferencesKey("lead_time_minutes")
        private val LAST_SYNC_TIME = longPreferencesKey("last_sync_time")
        private val MUTED_EVENTS = stringSetPreferencesKey("muted_events")
    }

    val autoSyncEnabled: Flow<Boolean> = context.dataStore.data
        .map { it[AUTO_SYNC_ENABLED] ?: true }

    val syncHour: Flow<Int> = context.dataStore.data
        .map { it[SYNC_HOUR] ?: 6 }

    val syncMinute: Flow<Int> = context.dataStore.data
        .map { it[SYNC_MINUTE] ?: 0 }

    val leadTimeMinutes: Flow<Int> = context.dataStore.data
        .map { it[LEAD_TIME_MINUTES] ?: 1 }

    val lastSyncTime: Flow<Long> = context.dataStore.data
        .map { it[LAST_SYNC_TIME] ?: 0L }

    val mutedEventIds: Flow<Set<String>> = context.dataStore.data
        .map { it[MUTED_EVENTS] ?: emptySet() }

    suspend fun setAutoSyncEnabled(enabled: Boolean) {
        context.dataStore.edit { it[AUTO_SYNC_ENABLED] = enabled }
    }

    suspend fun setSyncTime(hour: Int, minute: Int) {
        context.dataStore.edit {
            it[SYNC_HOUR] = hour
            it[SYNC_MINUTE] = minute
        }
    }

    suspend fun setLeadTimeMinutes(minutes: Int) {
        context.dataStore.edit { it[LEAD_TIME_MINUTES] = minutes }
    }

    suspend fun setLastSyncTime(time: Long) {
        context.dataStore.edit { it[LAST_SYNC_TIME] = time }
    }

    suspend fun toggleMutedEvent(eventId: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[MUTED_EVENTS] ?: emptySet()
            prefs[MUTED_EVENTS] = if (eventId in current) {
                current - eventId
            } else {
                current + eventId
            }
        }
    }

    suspend fun getMutedEventIds(): Set<String> {
        return context.dataStore.data.first()[MUTED_EVENTS] ?: emptySet()
    }

    suspend fun getLeadTimeMinutes(): Int {
        return context.dataStore.data.first()[LEAD_TIME_MINUTES] ?: 1
    }

    suspend fun getSyncHour(): Int {
        return context.dataStore.data.first()[SYNC_HOUR] ?: 6
    }

    suspend fun getSyncMinute(): Int {
        return context.dataStore.data.first()[SYNC_MINUTE] ?: 0
    }

    suspend fun isAutoSyncEnabled(): Boolean {
        return context.dataStore.data.first()[AUTO_SYNC_ENABLED] ?: true
    }
}
