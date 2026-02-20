package com.tungnk123.soundalarm.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import com.tungnk123.soundalarm.domain.model.AppSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsDataStoreManager @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _settingsFlow = MutableStateFlow(AppSettings())
    val settingsFlow: StateFlow<AppSettings> = _settingsFlow

    init {
        scope.launch {
            migrateDefaultsIfNeeded()
            dataStore.data
                .map { prefs -> prefs.toAppSettings() }
                .collect { _settingsFlow.value = it }
        }
    }

    private suspend fun migrateDefaultsIfNeeded() {
        dataStore.edit { prefs ->
            if (prefs[Keys.DEFAULTS_INITIALIZED] != true) {
                prefs[Keys.PRE_ALARM_NOTIFICATION] = AppSettings.DEFAULT_PRE_ALARM_NOTIFICATION
                prefs[Keys.ALARM_WHEN_POWER_OFF] = AppSettings.DEFAULT_ALARM_WHEN_POWER_OFF
                prefs[Keys.DEFAULTS_INITIALIZED] = true
            }
        }
    }

    fun getSettings(): AppSettings = _settingsFlow.value

    suspend fun updateSnoozeCount(count: Int) {
        dataStore.edit { it[Keys.SNOOZE_COUNT] = count }
    }

    suspend fun updateAlarmDuration(minutes: Int) {
        dataStore.edit { it[Keys.ALARM_DURATION] = minutes }
    }

    suspend fun updatePreAlarmNotification(enabled: Boolean) {
        dataStore.edit { it[Keys.PRE_ALARM_NOTIFICATION] = enabled }
    }

    suspend fun updatePreAlarmNotificationTime(minutes: Int) {
        dataStore.edit { it[Keys.PRE_ALARM_NOTIFICATION_TIME] = minutes }
    }

    suspend fun updateAlarmWhenPowerOff(enabled: Boolean) {
        dataStore.edit { it[Keys.ALARM_WHEN_POWER_OFF] = enabled }
    }

    suspend fun updateDefaultVolume(volume: Float) {
        dataStore.edit { it[Keys.DEFAULT_VOLUME] = volume }
    }

    suspend fun updateDefaultFadeInDuration(seconds: Int) {
        dataStore.edit { it[Keys.DEFAULT_FADE_IN_DURATION] = seconds }
    }

    suspend fun updateDefaultFadeOutDuration(seconds: Int) {
        dataStore.edit { it[Keys.DEFAULT_FADE_OUT_DURATION] = seconds }
    }

    suspend fun updateDefaultPlaylistId(id: Long) {
        dataStore.edit { it[Keys.DEFAULT_PLAYLIST_ID] = id }
    }

    private fun Preferences.toAppSettings() = AppSettings(
        snoozeCount = this[Keys.SNOOZE_COUNT] ?: AppSettings.DEFAULT_SNOOZE_COUNT,
        alarmDuration = this[Keys.ALARM_DURATION] ?: AppSettings.DEFAULT_ALARM_DURATION,
        preAlarmNotification = this[Keys.PRE_ALARM_NOTIFICATION] ?: AppSettings.DEFAULT_PRE_ALARM_NOTIFICATION,
        preAlarmNotificationTime = this[Keys.PRE_ALARM_NOTIFICATION_TIME] ?: AppSettings.DEFAULT_PRE_ALARM_TIME,
        alarmWhenPowerOff = this[Keys.ALARM_WHEN_POWER_OFF] ?: AppSettings.DEFAULT_ALARM_WHEN_POWER_OFF,
        defaultVolume = this[Keys.DEFAULT_VOLUME] ?: AppSettings.DEFAULT_VOLUME,
        defaultFadeInDuration = this[Keys.DEFAULT_FADE_IN_DURATION] ?: AppSettings.DEFAULT_FADE_IN_DURATION,
        defaultFadeOutDuration = this[Keys.DEFAULT_FADE_OUT_DURATION] ?: AppSettings.DEFAULT_FADE_OUT_DURATION,
        defaultPlaylistId = this[Keys.DEFAULT_PLAYLIST_ID] ?: AppSettings.NO_DEFAULT_PLAYLIST,
    )

    object Keys {
        val SNOOZE_COUNT = intPreferencesKey("snooze_count")
        val ALARM_DURATION = intPreferencesKey("alarm_duration")
        val PRE_ALARM_NOTIFICATION = booleanPreferencesKey("pre_alarm_notification")
        val PRE_ALARM_NOTIFICATION_TIME = intPreferencesKey("pre_alarm_notification_time")
        val ALARM_WHEN_POWER_OFF = booleanPreferencesKey("alarm_when_power_off")
        val DEFAULT_VOLUME = floatPreferencesKey("default_volume")
        val DEFAULT_FADE_IN_DURATION = intPreferencesKey("default_fade_in_duration")
        val DEFAULT_FADE_OUT_DURATION = intPreferencesKey("default_fade_out_duration")
        val DEFAULT_PLAYLIST_ID = longPreferencesKey("default_playlist_id")
        val DEFAULTS_INITIALIZED = booleanPreferencesKey("defaults_initialized")
    }
}
