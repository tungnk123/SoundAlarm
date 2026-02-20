package com.tungnk123.soundalarm.data.repository

import com.tungnk123.soundalarm.data.local.datastore.SettingsDataStoreManager
import com.tungnk123.soundalarm.domain.model.AppSettings
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
}
