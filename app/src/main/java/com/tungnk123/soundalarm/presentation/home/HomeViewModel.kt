package com.tungnk123.soundalarm.presentation.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tungnk123.soundalarm.R
import com.tungnk123.soundalarm.domain.model.Alarm
import com.tungnk123.soundalarm.domain.model.DayOfWeek
import com.tungnk123.soundalarm.domain.snooze.SnoozeManager
import com.tungnk123.soundalarm.domain.snooze.SnoozeState
import com.tungnk123.soundalarm.domain.usecase.DeleteAlarmUseCase
import com.tungnk123.soundalarm.domain.usecase.GetAlarmsUseCase
import com.tungnk123.soundalarm.domain.usecase.ToggleAlarmUseCase
import com.tungnk123.soundalarm.presentation.service.AlarmService
import com.tungnk123.soundalarm.presentation.scheduler.AlarmReceiver
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class HomeUiState(
    val alarms: List<Alarm> = emptyList(),
    val isLoading: Boolean = true,
    val nextAlarmText: String? = null,
    val snoozeState: SnoozeState? = null,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    getAlarmsUseCase: GetAlarmsUseCase,
    private val toggleAlarmUseCase: ToggleAlarmUseCase,
    private val deleteAlarmUseCase: DeleteAlarmUseCase,
    private val snoozeManager: SnoozeManager,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        getAlarmsUseCase(),
        snoozeManager.snoozeState,
    ) { alarms, snoozeState ->
        HomeUiState(
            alarms = alarms,
            isLoading = false,
            nextAlarmText = computeNextAlarmText(alarms),
            snoozeState = snoozeState,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

    fun toggleAlarm(id: Long, isEnabled: Boolean) {
        viewModelScope.launch {
            toggleAlarmUseCase(id, isEnabled)
        }
    }

    fun deleteAlarm(alarm: Alarm) {
        viewModelScope.launch {
            deleteAlarmUseCase(alarm)
        }
    }

    fun cancelSnooze() {
        val state = snoozeManager.snoozeState.value ?: return
        val cancelIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmService.ACTION_CANCEL_SNOOZE
            putExtra(AlarmService.EXTRA_ALARM_ID, state.alarmId)
            putExtra(AlarmService.EXTRA_ALARM_LABEL, state.alarmLabel)
        }
        context.sendBroadcast(cancelIntent)
    }

    private fun computeNextAlarmText(alarms: List<Alarm>): String? {
        val now = Calendar.getInstance()
        var minMillis = Long.MAX_VALUE

        for (alarm in alarms) {
            if (!alarm.isEnabled) continue
            if (alarm.repeatDays.isEmpty()) {
                val cal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, alarm.hour)
                    set(Calendar.MINUTE, alarm.minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                if (cal.timeInMillis <= now.timeInMillis) {
                    cal.add(Calendar.DAY_OF_YEAR, 1)
                }
                val diff = cal.timeInMillis - now.timeInMillis
                if (diff < minMillis) minMillis = diff
            } else {
                for (day in alarm.repeatDays) {
                    val calDay = when (day) {
                        DayOfWeek.MONDAY -> Calendar.MONDAY
                        DayOfWeek.TUESDAY -> Calendar.TUESDAY
                        DayOfWeek.WEDNESDAY -> Calendar.WEDNESDAY
                        DayOfWeek.THURSDAY -> Calendar.THURSDAY
                        DayOfWeek.FRIDAY -> Calendar.FRIDAY
                        DayOfWeek.SATURDAY -> Calendar.SATURDAY
                        DayOfWeek.SUNDAY -> Calendar.SUNDAY
                    }
                    val cal = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, alarm.hour)
                        set(Calendar.MINUTE, alarm.minute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val currentDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
                    var daysUntil = calDay - currentDayOfWeek
                    if (daysUntil < 0) daysUntil += 7
                    if (daysUntil == 0 && cal.timeInMillis <= now.timeInMillis) daysUntil = 7
                    cal.add(Calendar.DAY_OF_YEAR, daysUntil)
                    val diff = cal.timeInMillis - now.timeInMillis
                    if (diff < minMillis) minMillis = diff
                }
            }
        }

        if (minMillis == Long.MAX_VALUE) return null
        val hours = minMillis / (1000 * 60 * 60)
        val minutes = (minMillis % (1000 * 60 * 60)) / (1000 * 60)
        return when {
            hours > 0 -> context.getString(R.string.message_next_alarm_hours, hours.toInt(), minutes.toInt())
            minutes > 0 -> context.getString(R.string.message_next_alarm_minutes, minutes.toInt())
            else -> context.getString(R.string.message_next_alarm_soon)
        }
    }
}
