package com.tungnk123.soundalarm.domain.model

data class AlarmStatistics(
    val totalFired: Int = 0,
    val totalDismissed: Int = 0,
    val totalSnoozed: Int = 0,
    val totalAlarms: Int = 0,
    val activeAlarms: Int = 0,
    val dismissByMethod: Map<DismissMethod, Int> = emptyMap(),
    val alarmsByMethod: Map<DismissMethod, Int> = emptyMap(),
    val recentEvents: List<AlarmEvent> = emptyList(),
)
