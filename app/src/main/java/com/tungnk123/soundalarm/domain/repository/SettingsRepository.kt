package com.tungnk123.soundalarm.domain.repository

import com.tungnk123.soundalarm.domain.model.AppSettings
import com.tungnk123.soundalarm.domain.model.MathDifficulty
import kotlinx.coroutines.flow.StateFlow

interface SettingsRepository {
    val settingsFlow: StateFlow<AppSettings>
    fun getSettings(): AppSettings
    suspend fun updateSnoozeCount(count: Int)
    suspend fun updateAlarmDuration(minutes: Int)
    suspend fun updatePreAlarmNotification(enabled: Boolean)
    suspend fun updatePreAlarmNotificationTime(minutes: Int)
    suspend fun updateAlarmWhenPowerOff(enabled: Boolean)
    suspend fun updateDefaultVolume(volume: Float)
    suspend fun updateDefaultFadeInDuration(seconds: Int)
    suspend fun updateDefaultFadeOutDuration(seconds: Int)
    suspend fun updateDefaultPlaylistId(id: Long)
    suspend fun updateReadTimeAloud(enabled: Boolean)
    suspend fun updateTimeAnnouncementTemplate(template: String)
    suspend fun updateCustomVoiceAudioPath(path: String)
    suspend fun updateVoiceBeforeMusic(enabled: Boolean)
    suspend fun updateMathDifficulty(difficulty: MathDifficulty)
    suspend fun updateMathProblemCount(count: Int)
    suspend fun updateShakeCount(count: Int)
    suspend fun updateWalkStepGoal(steps: Int)
    suspend fun updateMemoryCodeLength(length: Int)
}
