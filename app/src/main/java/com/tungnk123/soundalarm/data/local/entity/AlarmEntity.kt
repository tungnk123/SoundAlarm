package com.tungnk123.soundalarm.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tungnk123.soundalarm.domain.model.Alarm
import com.tungnk123.soundalarm.domain.model.DayOfWeek
import com.tungnk123.soundalarm.domain.model.DismissMethod

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hour: Int,
    val minute: Int,
    val label: String = "",
    val isEnabled: Boolean = true,
    val repeatDays: String = "",
    val soundUri: String? = null,
    val isVibrate: Boolean = true,
    val deleteAfterFired: Boolean = false,
    val isRandomMusic: Boolean = false,
    val volume: Float = 1.0f,
    val fadeInDuration: Int = 0,
    val playlistGroupId: Long = 0,
    val dismissMethod: String = "NONE",
    val notifyBeforeMinutes: Int = 0,
) {
    fun toDomain(): Alarm = Alarm(
        id = id,
        hour = hour,
        minute = minute,
        label = label,
        isEnabled = isEnabled,
        repeatDays = repeatDays.split(",")
            .filter { it.isNotBlank() }
            .map { DayOfWeek.valueOf(it) }
            .toSet(),
        soundUri = soundUri,
        isVibrate = isVibrate,
        deleteAfterFired = deleteAfterFired,
        isRandomMusic = isRandomMusic,
        volume = volume,
        fadeInDuration = fadeInDuration,
        playlistGroupId = playlistGroupId,
        dismissMethod = runCatching { DismissMethod.valueOf(dismissMethod) }.getOrDefault(DismissMethod.NONE),
        notifyBeforeMinutes = notifyBeforeMinutes,
    )

    companion object {
        fun fromDomain(alarm: Alarm): AlarmEntity = AlarmEntity(
            id = alarm.id,
            hour = alarm.hour,
            minute = alarm.minute,
            label = alarm.label,
            isEnabled = alarm.isEnabled,
            repeatDays = alarm.repeatDays.joinToString(",") { it.name },
            soundUri = alarm.soundUri,
            isVibrate = alarm.isVibrate,
            deleteAfterFired = alarm.deleteAfterFired,
            isRandomMusic = alarm.isRandomMusic,
            volume = alarm.volume,
            fadeInDuration = alarm.fadeInDuration,
            playlistGroupId = alarm.playlistGroupId,
            dismissMethod = alarm.dismissMethod.name,
            notifyBeforeMinutes = alarm.notifyBeforeMinutes,
        )
    }
}
