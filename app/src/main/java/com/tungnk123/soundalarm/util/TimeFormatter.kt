package com.tungnk123.soundalarm.util

import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime

object TimeFormatter {

    fun formatTimeUntilAlarm(hour: Int, minute: Int): String {
        val now = LocalDateTime.now()
        var alarmTime = now.toLocalDate().atTime(LocalTime.of(hour, minute))
        if (alarmTime.isBefore(now)) {
            alarmTime = alarmTime.plusDays(1)
        }
        val duration = Duration.between(now, alarmTime)
        val hours = duration.toHours()
        val minutes = duration.toMinutes() % 60
        return when {
            hours > 0 -> "${hours}h ${minutes}m from now"
            else -> "${minutes}m from now"
        }
    }
}
