package com.tungnk123.soundalarm.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.tungnk123.soundalarm.domain.model.AppSettings
import com.tungnk123.soundalarm.domain.model.MathDifficulty
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

    suspend fun updateReadTimeAloud(enabled: Boolean) {
        dataStore.edit { it[Keys.READ_TIME_ALOUD] = enabled }
    }

    suspend fun updateTimeAnnouncementTemplate(template: String) {
        dataStore.edit { it[Keys.TIME_ANNOUNCEMENT_TEMPLATE] = template }
    }

    suspend fun updateCustomVoiceAudioPath(path: String) {
        dataStore.edit { it[Keys.CUSTOM_VOICE_AUDIO_PATH] = path }
    }

    suspend fun updateVoiceBeforeMusic(enabled: Boolean) {
        dataStore.edit { it[Keys.VOICE_BEFORE_MUSIC] = enabled }
    }

    suspend fun updateMathDifficulty(difficulty: MathDifficulty) {
        dataStore.edit { it[Keys.MATH_DIFFICULTY] = difficulty.name }
    }

    suspend fun updateMathProblemCount(count: Int) {
        dataStore.edit { it[Keys.MATH_PROBLEM_COUNT] = count }
    }

    suspend fun updateShakeCount(count: Int) {
        dataStore.edit { it[Keys.SHAKE_COUNT] = count }
    }

    suspend fun updateWalkStepGoal(steps: Int) {
        dataStore.edit { it[Keys.WALK_STEP_GOAL] = steps }
    }

    suspend fun updateMemoryCodeLength(length: Int) {
        dataStore.edit { it[Keys.MEMORY_CODE_LENGTH] = length }
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
        readTimeAloud = this[Keys.READ_TIME_ALOUD] ?: AppSettings.DEFAULT_READ_TIME_ALOUD,
        timeAnnouncementTemplate = this[Keys.TIME_ANNOUNCEMENT_TEMPLATE] ?: AppSettings.DEFAULT_TIME_ANNOUNCEMENT_TEMPLATE,
        customVoiceAudioPath = this[Keys.CUSTOM_VOICE_AUDIO_PATH] ?: AppSettings.DEFAULT_CUSTOM_VOICE_AUDIO_PATH,
        voiceBeforeMusic = this[Keys.VOICE_BEFORE_MUSIC] ?: AppSettings.DEFAULT_VOICE_BEFORE_MUSIC,
        mathDifficulty = runCatching {
            MathDifficulty.valueOf(this[Keys.MATH_DIFFICULTY] ?: MathDifficulty.EASY.name)
        }.getOrDefault(MathDifficulty.EASY),
        mathProblemCount = this[Keys.MATH_PROBLEM_COUNT] ?: AppSettings.DEFAULT_MATH_PROBLEM_COUNT,
        shakeCount = this[Keys.SHAKE_COUNT] ?: AppSettings.DEFAULT_SHAKE_COUNT,
        walkStepGoal = this[Keys.WALK_STEP_GOAL] ?: AppSettings.DEFAULT_WALK_STEP_GOAL,
        memoryCodeLength = this[Keys.MEMORY_CODE_LENGTH] ?: AppSettings.DEFAULT_MEMORY_CODE_LENGTH,
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
        val READ_TIME_ALOUD = booleanPreferencesKey("read_time_aloud")
        val TIME_ANNOUNCEMENT_TEMPLATE = stringPreferencesKey("time_announcement_template")
        val CUSTOM_VOICE_AUDIO_PATH = stringPreferencesKey("custom_voice_audio_path")
        val VOICE_BEFORE_MUSIC = booleanPreferencesKey("voice_before_music")
        val MATH_DIFFICULTY = stringPreferencesKey("math_difficulty")
        val MATH_PROBLEM_COUNT = intPreferencesKey("math_problem_count")
        val SHAKE_COUNT = intPreferencesKey("shake_count")
        val WALK_STEP_GOAL = intPreferencesKey("walk_step_goal")
        val MEMORY_CODE_LENGTH = intPreferencesKey("memory_code_length")
    }
}
