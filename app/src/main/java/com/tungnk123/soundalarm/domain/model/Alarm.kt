package com.tungnk123.soundalarm.domain.model

data class Alarm(
    val id: Long = 0,
    val hour: Int,
    val minute: Int,
    val label: String = "",
    val isEnabled: Boolean = true,
    val repeatDays: Set<DayOfWeek> = emptySet(),
    val soundUri: String? = null,
    val isVibrate: Boolean = true,
    val deleteAfterFired: Boolean = false,
    val isRandomMusic: Boolean = false,
    val volume: Float = 1.0f,
    val fadeInDuration: Int = 0,
)

enum class DayOfWeek(val shortName: String) {
    MONDAY("Mon"),
    TUESDAY("Tue"),
    WEDNESDAY("Wed"),
    THURSDAY("Thu"),
    FRIDAY("Fri"),
    SATURDAY("Sat"),
    SUNDAY("Sun"),
}
