package com.tungnk123.soundalarm.domain.model

import com.tungnk123.soundalarm.data.local.entity.AlarmEventEntity

enum class AlarmEventType { FIRED, DISMISSED, SNOOZED }

data class AlarmEvent(
    val id: Long,
    val alarmId: Long,
    val alarmLabel: String,
    val eventType: AlarmEventType,
    val dismissMethod: DismissMethod,
    val timestamp: Long,
)

fun AlarmEventEntity.toDomain() = AlarmEvent(
    id = id,
    alarmId = alarmId,
    alarmLabel = alarmLabel,
    eventType = runCatching { AlarmEventType.valueOf(eventType) }.getOrDefault(AlarmEventType.FIRED),
    dismissMethod = runCatching { DismissMethod.valueOf(dismissMethod) }.getOrDefault(DismissMethod.NONE),
    timestamp = timestamp,
)
