package com.tungnk123.soundalarm.domain.snooze

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class SnoozeState(
    val alarmId: Long,
    val alarmLabel: String,
    val snoozeUntilMs: Long,
)

@Singleton
class SnoozeManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val prefs = context.getSharedPreferences("snooze_prefs", Context.MODE_PRIVATE)

    private val _snoozeState = MutableStateFlow(loadFromPrefs())
    val snoozeState: StateFlow<SnoozeState?> = _snoozeState

    fun setSnooze(alarmId: Long, alarmLabel: String, snoozeUntilMs: Long) {
        val newCount = getSnoozeCount(alarmId) + 1
        prefs.edit {
            putBoolean("active", true)
            putLong("alarm_id", alarmId)
            putString("alarm_label", alarmLabel)
            putLong("snooze_until_ms", snoozeUntilMs)
            putInt("snooze_count_$alarmId", newCount)
        }
        _snoozeState.value = SnoozeState(alarmId, alarmLabel, snoozeUntilMs)
    }

    fun getSnoozeCount(alarmId: Long): Int =
        prefs.getInt("snooze_count_$alarmId", 0)

    fun clearSnooze() {
        prefs.edit {
            remove("active")
            remove("alarm_id")
            remove("alarm_label")
            remove("snooze_until_ms")
        }
        _snoozeState.value = null
    }

    fun resetSnoozeCount(alarmId: Long) {
        if (alarmId != -1L) {
            prefs.edit { remove("snooze_count_$alarmId") }
        }
    }

    private fun loadFromPrefs(): SnoozeState? {
        if (!prefs.getBoolean("active", false)) return null
        return SnoozeState(
            alarmId = prefs.getLong("alarm_id", -1L),
            alarmLabel = prefs.getString("alarm_label", "Alarm") ?: "Alarm",
            snoozeUntilMs = prefs.getLong("snooze_until_ms", 0L),
        )
    }
}
