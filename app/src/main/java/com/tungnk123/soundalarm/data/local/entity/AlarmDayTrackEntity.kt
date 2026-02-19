package com.tungnk123.soundalarm.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "alarm_day_tracks",
    foreignKeys = [
        ForeignKey(
            entity = AlarmEntity::class,
            parentColumns = ["id"],
            childColumns = ["alarmId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index(value = ["alarmId"])],
)
data class AlarmDayTrackEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val alarmId: Long,
    // DayOfWeek.name (e.g. "MONDAY") or "DEFAULT" for the fallback track
    val dayOfWeek: String,
    val trackUri: String,
    val trackTitle: String,
    val trackArtist: String,
)
