package com.tungnk123.soundalarm.domain.usecase

import com.tungnk123.soundalarm.domain.model.Alarm
import com.tungnk123.soundalarm.domain.repository.AlarmRepository
import javax.inject.Inject

class DeleteAlarmUseCase @Inject constructor(
    private val repository: AlarmRepository,
    private val scheduler: com.tungnk123.soundalarm.domain.scheduler.AlarmScheduler,
) {
    suspend operator fun invoke(alarm: Alarm) {
        scheduler.cancel(alarm)
        repository.deleteAlarm(alarm)
    }
}
