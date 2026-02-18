package com.tungnk123.soundalarm.domain.usecase

import com.tungnk123.soundalarm.domain.model.Alarm
import com.tungnk123.soundalarm.domain.repository.AlarmRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAlarmsUseCase @Inject constructor(
    private val repository: AlarmRepository,
) {
    operator fun invoke(): Flow<List<Alarm>> = repository.getAllAlarms()
}
