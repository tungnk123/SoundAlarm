package com.tungnk123.soundalarm.presentation.challenge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tungnk123.soundalarm.domain.model.AppSettings
import com.tungnk123.soundalarm.domain.model.MathDifficulty
import com.tungnk123.soundalarm.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChallengeViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.settingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = settingsRepository.getSettings(),
        )

    fun updateMathDifficulty(difficulty: MathDifficulty) {
        viewModelScope.launch { settingsRepository.updateMathDifficulty(difficulty) }
    }

    fun updateMathProblemCount(count: Int) {
        viewModelScope.launch { settingsRepository.updateMathProblemCount(count) }
    }

    fun updateShakeCount(count: Int) {
        viewModelScope.launch { settingsRepository.updateShakeCount(count) }
    }

    fun updateWalkStepGoal(steps: Int) {
        viewModelScope.launch { settingsRepository.updateWalkStepGoal(steps) }
    }
}
