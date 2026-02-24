package com.tungnk123.soundalarm.data.repository

import com.tungnk123.soundalarm.data.local.datastore.SettingsDataStoreManager
import com.tungnk123.soundalarm.domain.model.AppSettings
import com.tungnk123.soundalarm.domain.model.MathDifficulty
import com.tungnk123.soundalarm.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val manager: SettingsDataStoreManager,
) : SettingsRepository {

    override val settingsFlow: StateFlow<AppSettings> = manager.settingsFlow

    override fun getSettings(): AppSettings = manager.getSettings()

    override suspend fun updateSnoozeCount(count: Int) = manager.updateSnoozeCount(count)

    override suspend fun updateAlarmDuration(minutes: Int) = manager.updateAlarmDuration(minutes)

    override suspend fun updatePreAlarmNotification(enabled: Boolean) =
        manager.updatePreAlarmNotification(enabled)

    override suspend fun updatePreAlarmNotificationTime(minutes: Int) =
        manager.updatePreAlarmNotificationTime(minutes)

    override suspend fun updateAlarmWhenPowerOff(enabled: Boolean) =
        manager.updateAlarmWhenPowerOff(enabled)

    override suspend fun updateDefaultVolume(volume: Float) = manager.updateDefaultVolume(volume)

    override suspend fun updateDefaultFadeInDuration(seconds: Int) =
        manager.updateDefaultFadeInDuration(seconds)

    override suspend fun updateDefaultFadeOutDuration(seconds: Int) =
        manager.updateDefaultFadeOutDuration(seconds)

    override suspend fun updateDefaultPlaylistId(id: Long) = manager.updateDefaultPlaylistId(id)

    override suspend fun updateReadTimeAloud(enabled: Boolean) = manager.updateReadTimeAloud(enabled)

    override suspend fun updateTimeAnnouncementTemplate(template: String) =
        manager.updateTimeAnnouncementTemplate(template)

    override suspend fun updateCustomVoiceAudioPath(path: String) =
        manager.updateCustomVoiceAudioPath(path)

    override suspend fun updateVoiceBeforeMusic(enabled: Boolean) =
        manager.updateVoiceBeforeMusic(enabled)

    override suspend fun updateMathDifficulty(difficulty: MathDifficulty) =
        manager.updateMathDifficulty(difficulty)

    override suspend fun updateMathProblemCount(count: Int) = manager.updateMathProblemCount(count)

    override suspend fun updateShakeCount(count: Int) = manager.updateShakeCount(count)

    override suspend fun updateWalkStepGoal(steps: Int) = manager.updateWalkStepGoal(steps)

    override suspend fun updateMemoryCodeLength(length: Int) = manager.updateMemoryCodeLength(length)
}
