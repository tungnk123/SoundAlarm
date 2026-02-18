package com.tungnk123.soundalarm.domain.model

data class AlarmDayTrack(
    val id: Long = 0,
    val alarmId: Long,
    // DayOfWeek.name (e.g. "MONDAY") or "DEFAULT" for the fallback track
    val dayOfWeek: String,
    val trackUri: String,
    val trackTitle: String,
    val trackArtist: String,
) {
    companion object {
        const val DEFAULT_DAY = "DEFAULT"
    }
}
