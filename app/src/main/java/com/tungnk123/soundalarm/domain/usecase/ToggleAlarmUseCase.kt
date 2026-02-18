package com.tungnk123.soundalarm.domain.usecase

import com.tungnk123.soundalarm.domain.repository.AlarmRepository
import javax.inject.Inject

class ToggleAlarmUseCase @Inject constructor(
    private val repository: AlarmRepository,
    private val scheduler: com.tungnk123.soundalarm.domain.scheduler.AlarmScheduler,
) {
    suspend operator fun invoke(id: Long, isEnabled: Boolean) {
        repository.toggleAlarm(id, isEnabled)
        val alarm = repository.getAlarmById(id)
        if (alarm != null) {
            if (isEnabled) {
                scheduler.schedule(alarm)
            } else {
                scheduler.cancel(alarm)
            }
        }
    }
}
