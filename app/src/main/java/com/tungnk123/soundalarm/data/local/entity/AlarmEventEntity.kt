package com.tungnk123.soundalarm.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarm_events")
data class AlarmEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val alarmId: Long,
    val alarmLabel: String,
    val eventType: String,
    val dismissMethod: String = "NONE",
    val timestamp: Long = System.currentTimeMillis(),
)
