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
        prefs.edit {
            putBoolean("active", true)
            putLong("alarm_id", alarmId)
            putString("alarm_label", alarmLabel)
            putLong("snooze_until_ms", snoozeUntilMs)
        }
        _snoozeState.value = SnoozeState(alarmId, alarmLabel, snoozeUntilMs)
    }

    fun clearSnooze() {
        prefs.edit { clear() }
        _snoozeState.value = null
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
