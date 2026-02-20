package com.tungnk123.soundalarm.domain.model

data class AppSettings(
    val snoozeCount: Int = DEFAULT_SNOOZE_COUNT,
    val alarmDuration: Int = DEFAULT_ALARM_DURATION,
    val preAlarmNotification: Boolean = DEFAULT_PRE_ALARM_NOTIFICATION,
    val preAlarmNotificationTime: Int = DEFAULT_PRE_ALARM_TIME,
    val alarmWhenPowerOff: Boolean = DEFAULT_ALARM_WHEN_POWER_OFF,
    val defaultVolume: Float = DEFAULT_VOLUME,
    val defaultFadeInDuration: Int = DEFAULT_FADE_IN_DURATION,
    val defaultFadeOutDuration: Int = DEFAULT_FADE_OUT_DURATION,
    val defaultPlaylistId: Long = NO_DEFAULT_PLAYLIST,
) {
    companion object {
        const val DEFAULT_SNOOZE_COUNT = 3
        const val DEFAULT_ALARM_DURATION = 10
        const val DEFAULT_PRE_ALARM_NOTIFICATION = true
        const val DEFAULT_PRE_ALARM_TIME = 10
        const val DEFAULT_ALARM_WHEN_POWER_OFF = true
        const val DEFAULT_VOLUME = 1.0f
        const val DEFAULT_FADE_IN_DURATION = 0
        const val DEFAULT_FADE_OUT_DURATION = 0
        const val NO_DEFAULT_PLAYLIST = -1L
    }
}
