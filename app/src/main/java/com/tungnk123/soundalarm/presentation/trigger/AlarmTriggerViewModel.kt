package com.tungnk123.soundalarm.presentation.trigger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tungnk123.soundalarm.domain.model.Alarm
import com.tungnk123.soundalarm.domain.model.AppSettings
import com.tungnk123.soundalarm.domain.model.DayOfWeek
import com.tungnk123.soundalarm.domain.model.DismissMethod
import com.tungnk123.soundalarm.domain.model.MathDifficulty
import com.tungnk123.soundalarm.domain.repository.AlarmRepository
import com.tungnk123.soundalarm.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class AlarmTriggerViewModel @Inject constructor(
    private val alarmRepository: AlarmRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    data class ChallengeConfig(
        val dismissMethod: DismissMethod = DismissMethod.NONE,
        val mathDifficulty: MathDifficulty = MathDifficulty.EASY,
        val mathProblemCount: Int = AppSettings.DEFAULT_MATH_PROBLEM_COUNT,
        val shakeCount: Int = AppSettings.DEFAULT_SHAKE_COUNT,
        val walkStepGoal: Int = AppSettings.DEFAULT_WALK_STEP_GOAL,
    )

    private val _nextAlarmText = MutableStateFlow<String?>(null)
    val nextAlarmText: StateFlow<String?> = _nextAlarmText.asStateFlow()

    private val _challengeConfig = MutableStateFlow(ChallengeConfig())
    val challengeConfig: StateFlow<ChallengeConfig> = _challengeConfig.asStateFlow()

    fun loadAlarm(alarmId: Long) {
        viewModelScope.launch {
            val alarm = if (alarmId != -1L) alarmRepository.getAlarmById(alarmId) else null
            val settings = settingsRepository.getSettings()
            _challengeConfig.value = ChallengeConfig(
                dismissMethod = alarm?.dismissMethod ?: DismissMethod.NONE,
                mathDifficulty = settings.mathDifficulty,
                mathProblemCount = settings.mathProblemCount,
                shakeCount = settings.shakeCount,
                walkStepGoal = settings.walkStepGoal,
            )
        }
    }

    fun loadNextAlarm(excludeAlarmId: Long) {
        viewModelScope.launch {
            val alarms = alarmRepository.getEnabledAlarms()
                .filter { it.id != excludeAlarmId }
            _nextAlarmText.value = computeNextAlarmText(alarms)
        }
    }

    private fun computeNextAlarmText(alarms: List<Alarm>): String? {
        val now = Calendar.getInstance()
        var minMillis = Long.MAX_VALUE

        for (alarm in alarms) {
            val fireMillis = nextFireTimeMillis(alarm, now)
            if (fireMillis < minMillis) minMillis = fireMillis
        }

        if (minMillis == Long.MAX_VALUE) return null

        val totalSeconds = minMillis / 1_000L
        val days = totalSeconds / 86_400L
        val hours = (totalSeconds % 86_400L) / 3_600L
        val mins = (totalSeconds % 3_600L) / 60L

        return when {
            days >= 1L && hours > 0L -> "${days}d ${hours}h"
            days >= 1L -> "${days}d"
            hours > 0L && mins > 0L -> "${hours}h ${mins}m"
            hours > 0L -> "${hours}h"
            mins > 0L -> "${mins}m"
            else -> "Soon"
        }
    }

    private fun nextFireTimeMillis(alarm: Alarm, now: Calendar): Long {
        return if (alarm.repeatDays.isEmpty()) {
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, alarm.hour)
                set(Calendar.MINUTE, alarm.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            if (cal.timeInMillis <= now.timeInMillis) {
                cal.add(Calendar.DAY_OF_YEAR, 1)
            }
            cal.timeInMillis - now.timeInMillis
        } else {
            alarm.repeatDays.minOf { day ->
                val calDay = dayOfWeekToCalendar(day)
                val cal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, alarm.hour)
                    set(Calendar.MINUTE, alarm.minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                var daysUntil = calDay - cal.get(Calendar.DAY_OF_WEEK)
                if (daysUntil < 0) daysUntil += 7
                if (daysUntil == 0 && cal.timeInMillis <= now.timeInMillis) daysUntil = 7
                cal.add(Calendar.DAY_OF_YEAR, daysUntil)
                cal.timeInMillis - now.timeInMillis
            }
        }
    }

    private fun dayOfWeekToCalendar(day: DayOfWeek): Int = when (day) {
        DayOfWeek.MONDAY -> Calendar.MONDAY
        DayOfWeek.TUESDAY -> Calendar.TUESDAY
        DayOfWeek.WEDNESDAY -> Calendar.WEDNESDAY
        DayOfWeek.THURSDAY -> Calendar.THURSDAY
        DayOfWeek.FRIDAY -> Calendar.FRIDAY
        DayOfWeek.SATURDAY -> Calendar.SATURDAY
        DayOfWeek.SUNDAY -> Calendar.SUNDAY
    }
}
