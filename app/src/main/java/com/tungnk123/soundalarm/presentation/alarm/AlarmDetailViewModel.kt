package com.tungnk123.soundalarm.presentation.alarm

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tungnk123.soundalarm.domain.model.Alarm
import com.tungnk123.soundalarm.domain.model.DayOfWeek
import com.tungnk123.soundalarm.domain.repository.AlarmRepository
import com.tungnk123.soundalarm.domain.usecase.SaveAlarmUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AlarmDetailUiState(
    val hour: Int = 8,
    val minute: Int = 0,
    val label: String = "",
    val repeatDays: Set<DayOfWeek> = emptySet(),
    val isVibrate: Boolean = true,
    val soundUri: String? = null,
    val isEditing: Boolean = false,
    val isSaved: Boolean = false,
)

@HiltViewModel
class AlarmDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val alarmRepository: AlarmRepository,
    private val saveAlarmUseCase: SaveAlarmUseCase,
) : ViewModel() {

    private val alarmId: Long = savedStateHandle["alarmId"] ?: 0L

    private val _uiState = MutableStateFlow(AlarmDetailUiState())
    val uiState: StateFlow<AlarmDetailUiState> = _uiState.asStateFlow()

    init {
        if (alarmId != 0L) {
            loadAlarm()
        }
    }

    private fun loadAlarm() {
        viewModelScope.launch {
            alarmRepository.getAlarmById(alarmId)?.let { alarm ->
                _uiState.update {
                    it.copy(
                        hour = alarm.hour,
                        minute = alarm.minute,
                        label = alarm.label,
                        repeatDays = alarm.repeatDays,
                        isVibrate = alarm.isVibrate,
                        soundUri = alarm.soundUri,
                        isEditing = true,
                    )
                }
            }
        }
    }

    fun updateTime(hour: Int, minute: Int) {
        _uiState.update { it.copy(hour = hour, minute = minute) }
    }

    fun updateLabel(label: String) {
        _uiState.update { it.copy(label = label) }
    }

    fun toggleDay(day: DayOfWeek) {
        _uiState.update { state ->
            val newDays = state.repeatDays.toMutableSet()
            if (day in newDays) newDays.remove(day) else newDays.add(day)
            state.copy(repeatDays = newDays)
        }
    }

    fun toggleVibrate() {
        _uiState.update { it.copy(isVibrate = !it.isVibrate) }
    }

    fun saveAlarm() {
        viewModelScope.launch {
            val state = _uiState.value
            val alarm = Alarm(
                id = alarmId,
                hour = state.hour,
                minute = state.minute,
                label = state.label,
                repeatDays = state.repeatDays,
                isVibrate = state.isVibrate,
                soundUri = state.soundUri,
            )
            saveAlarmUseCase(alarm)
            _uiState.update { it.copy(isSaved = true) }
        }
    }
}
