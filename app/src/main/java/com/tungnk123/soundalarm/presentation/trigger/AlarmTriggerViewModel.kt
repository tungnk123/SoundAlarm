package com.tungnk123.soundalarm.presentation.trigger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tungnk123.soundalarm.domain.model.Alarm
import com.tungnk123.soundalarm.domain.model.DayOfWeek
import com.tungnk123.soundalarm.domain.repository.AlarmRepository
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
) : ViewModel() {

    private val _nextAlarmText = MutableStateFlow<String?>(null)
    val nextAlarmText: StateFlow<String?> = _nextAlarmText.asStateFlow()

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

        // Use Long arithmetic throughout — no Int conversion until final formatting
        val totalSeconds = minMillis / 1_000L
        val days  = totalSeconds / 86_400L
        val hours = (totalSeconds % 86_400L) / 3_600L
        val mins  = (totalSeconds % 3_600L) / 60L

        return when {
            days >= 1L && hours > 0L -> "${days}d ${hours}h"
            days >= 1L               -> "${days}d"
            hours > 0L && mins > 0L  -> "${hours}h ${mins}m"
            hours > 0L               -> "${hours}h"
            mins > 0L                -> "${mins}m"
            else                     -> "Soon"
        }
    }

    private fun nextFireTimeMillis(alarm: Alarm, now: Calendar): Long {
        return if (alarm.repeatDays.isEmpty()) {
            // One-time alarm: use today, push to tomorrow if already passed
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
            // Repeating alarm: find the soonest matching day
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
        DayOfWeek.MONDAY    -> Calendar.MONDAY
        DayOfWeek.TUESDAY   -> Calendar.TUESDAY
        DayOfWeek.WEDNESDAY -> Calendar.WEDNESDAY
        DayOfWeek.THURSDAY  -> Calendar.THURSDAY
        DayOfWeek.FRIDAY    -> Calendar.FRIDAY
        DayOfWeek.SATURDAY  -> Calendar.SATURDAY
        DayOfWeek.SUNDAY    -> Calendar.SUNDAY
    }
}
