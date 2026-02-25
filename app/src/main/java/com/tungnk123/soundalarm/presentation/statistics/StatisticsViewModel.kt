package com.tungnk123.soundalarm.presentation.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tungnk123.soundalarm.domain.model.AlarmStatistics
import com.tungnk123.soundalarm.domain.model.DismissMethod
import com.tungnk123.soundalarm.domain.repository.AlarmRepository
import com.tungnk123.soundalarm.domain.repository.StatisticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val statisticsRepository: StatisticsRepository,
    private val alarmRepository: AlarmRepository,
) : ViewModel() {

    val uiState: StateFlow<AlarmStatistics> = combine(
        statisticsRepository.getStatistics(),
        alarmRepository.getAllAlarms(),
    ) { stats, alarms ->
        stats.copy(
            totalAlarms = alarms.size,
            activeAlarms = alarms.count { it.isEnabled },
            alarmsByMethod = DismissMethod.entries.associateWith { method ->
                alarms.count { it.dismissMethod == method }
            },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AlarmStatistics(),
    )

    fun clearStatistics() {
        viewModelScope.launch {
            statisticsRepository.clearStatistics()
        }
    }
}
